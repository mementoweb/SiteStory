
/*
 *  mod_sitestory.c
 *  
 *
 *  Created by Dr. Lyudmila Balakireva on 6/15/09.
 *  Copyright (c) 2012, Los Alamos National Security, LLC All rights reserved.
 *  BSD open source software license. 
 *
 */
#include "httpd.h"
#include "http_connection.h"
#include "http_config.h"
#include "http_core.h"
#include "http_log.h"
#include "apr_strings.h"
#include "apr_tables.h"
#include <apr_network_io.h>
#include "util_ebcdic.h"
#include "apr_hash.h"
#include "apr_thread_mutex.h"
#include "apr_time.h"
#include "ap_mpm.h"

#define HEADEREND CRLF CRLF
#define ASCII_ZERO  "\060"
#define ASCII_CRLF  "\015\012"
/* default buffer size */
#define BUFSIZE			4096
#define TABLE_INIT_SZ           32
#define ap_is_HTTP_CODE_ALLOW(x) (((x) == 302) || ((x) == 200) || ((x) == 303))
//const char *authHost = "memento.lanl.gov";
//static const char *tabaseurl= "/tomcat/ta/";
const char *srv ;
//char *headers;
apr_table_t  *mytable=NULL;
 
typedef struct  {
	apr_socket_t *sock; 
	char *authRequest;
	char *headers;
        char *puturl;
	int index;
        apr_size_t authSize;
	} s_ctx;

typedef struct {
  const char *archiveurl;
  const char *host;
  const char  *tgurl; 
  //apr_port_t port;
  const char *port;
  int enable_ta;
  int enable_ip;
  apr_array_header_t *excluded_dirs;
  //  apr_global_mutex_t *mutex;
} ta_cfg;




static ta_cfg* conf;
static int threaded_mpm;

module AP_MODULE_DECLARE_DATA sitestory_module;


/* Per-dir config initialisation */
static void* put_config(apr_pool_t* pool, char* x) {
  ta_cfg *ptr = apr_pcalloc(pool, sizeof(ta_cfg)) ;
  ptr->enable_ta = 0;
  ptr->enable_ip = 0;
  ptr->excluded_dirs = NULL;
  //ptr->mutex = NULL;
  return ptr;
}



                                                                              

int iterate_func(void *req, const char *key, const char *value) {
    
    char *line;
   
    //request_rec *r = (request_rec *)req;
    ap_filter_t *f =(ap_filter_t *) req;
     request_rec *r = f->r;
     s_ctx* sctx = (s_ctx*) f->ctx;
    
    if (key == NULL || value == NULL || value[0] == '\0' || key[0]=='\0') return 1;
             ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "mod_sitestory: in the iterate function%s:%s",key,value) ;
             line = apr_pstrcat(r->pool,key,":",value,"\r\n",NULL);
	
	      if (!sctx->headers) {
		sctx->headers = apr_pstrdup(r->pool,"");
	      }
	      sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);
	
    return 1;
}

int get_headers( void *req, apr_table_t *tab ) {

     ap_filter_t *f =(ap_filter_t *) req;
     request_rec *r = f->r;
     s_ctx* sctx = (s_ctx*) f->ctx;
     char *line;


     if (!sctx->headers) {
       sctx->headers = apr_pstrdup(r->pool,"");
     }

    if (apr_table_get(tab,"User-Agent")) {
        line = apr_pstrcat(r->pool,"User-Agent:",apr_table_get(tab,"User-Agent"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL); 
     }

    
    if (apr_table_get(tab,"Referer")) {
        line = apr_pstrcat(r->pool,"Referer:",apr_table_get(tab,"Referer"),"\r\n",NULL);
	sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);      
    }

    if (apr_table_get(tab,"Accept")) {
        line = apr_pstrcat(r->pool,"Accept:",apr_table_get(tab,"Accept"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);
    }

    if (apr_table_get(tab,"Accept-Language")) {
         line = apr_pstrcat(r->pool,"Accept-Language:",apr_table_get(tab,"Accept-Language"),"\r\n",NULL);
         sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);     
    }

    if (apr_table_get(tab,"Accept-Encoding")) {
        line = apr_pstrcat(r->pool,"Accept-Encoding:",apr_table_get(tab,"Accept-Encoding"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);
    }

    if (apr_table_get(tab,"Cookie")) {
        line = apr_pstrcat(r->pool,"Cookie:",apr_table_get(tab,"Cookie"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);
      }

    if (apr_table_get(tab,"Connection")) {
        line = apr_pstrcat(r->pool,"Connection:",apr_table_get(tab,"Connection"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);
     }

    if (apr_table_get(tab,"Host")) {
        line = apr_pstrcat(r->pool,"Host:",apr_table_get(tab,"Host"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL); 
    }

    if (apr_table_get(tab,"Accept-Charset")) {
        line = apr_pstrcat(r->pool,"Accept-Charset:",apr_table_get(tab,"Accept-Charset"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);   
    }

    if (apr_table_get(tab,"If-Modified-Since")) {
        line = apr_pstrcat(r->pool,"If-Modified-Since:",apr_table_get(tab,"If-Modified-Since"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);
    }

    if (apr_table_get(tab,"If-None-Match")) {
       line = apr_pstrcat(r->pool,"If-None-Match:",apr_table_get(tab,"If-None-Match"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);
    }

    if (apr_table_get(tab,"Cache-Control")) {
        line = apr_pstrcat(r->pool,"Cache-Control:",apr_table_get(tab,"Cache-Control"),"\r\n",NULL);
        sctx->headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);      
    }

    return 1;

}



static apr_status_t ta_out_filter3(ap_filter_t *f,apr_bucket_brigade *bb){
	request_rec *r = f->r;
	//     conn_rec  *c =  f->c;
	apr_sockaddr_t *sockaddr;
	apr_status_t status;
	apr_interval_time_t timeout = 25000000; //25 sec
	//apr_interval_time_t timeout = 500000000;//500 sec	
	//apr_size_t len;
	apr_bucket* b;
        //int allow = 0;
        
	if (!ap_is_HTTP_CODE_ALLOW(r->status)) {  ap_remove_output_filter(f); return ap_pass_brigade(f->next, bb) ; }
	if (r->method_number != M_GET){  ap_remove_output_filter(f); return ap_pass_brigade(f->next, bb) ; }

	 s_ctx* sctx = (s_ctx*) f->ctx; 
	
       
	if (sctx  == NULL) {
		 ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r, "mod_sitestory: in the init section   filter function") ;
	      
		 sctx = f->ctx = apr_pcalloc(r->pool, sizeof(s_ctx));
		 apr_pool_cleanup_null(sctx->headers);
		
		 //apr_table_do(iterate_func, f, mytable, NULL);
                 char* creq = r->the_request; 		 
		 apr_pool_t *spool;

		 apr_pool_create(&spool, r->pool);
		 sctx->headers = NULL;    
		 if (!sctx->headers) {
		   sctx->headers = apr_pstrdup(r->pool,"");
		 }

                 if (creq!=NULL) {
		 sctx-> headers = apr_pstrcat(r->pool,sctx->headers,creq,"\r\n",NULL);
		 }
		 get_headers(f, r->headers_in); 
		 
                 if (conf->enable_ip){
                 char *remote_ip=r->connection->remote_ip;
                 char *line = apr_psprintf(r->pool, "%s:%s\r\n", "X-Client-IP", remote_ip);
                 sctx-> headers = apr_pstrcat(r->pool,sctx->headers,line,NULL);
		 }
		 
	         ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: in headers  request  %s", r->connection->remote_ip) ;
		 ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: in headers  request  %s",sctx->headers) ;
	   
		const char *uri = r->unparsed_uri;
		ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: in headers  request  %s",uri) ;

		
	       
                 sctx->puturl = apr_pstrcat(r->pool,conf->archiveurl,"http://",srv,uri,NULL); 
                 sctx->authSize=0;
		 apr_port_t port = (apr_port_t) atoi(conf->port);

		if ((status = apr_sockaddr_info_get(&sockaddr, conf->host, APR_INET, port, 0, r->pool)) != APR_SUCCESS) {
			 ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory:problem creating socket address"  ) ;
                         ap_remove_output_filter(f);
			 return ap_pass_brigade(f->next, bb) ;				
		}
		
		if ((status = apr_socket_create(&(sctx->sock), sockaddr->family, SOCK_STREAM, APR_PROTO_TCP, r->pool)) != APR_SUCCESS) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory:problem creating socket"  ) ;
                        ap_remove_output_filter(f);
			 return ap_pass_brigade(f->next, bb) ;
		}
		       
			if((status= apr_socket_opt_set(sctx->sock, APR_SO_NONBLOCK, 1 ))!= APR_SUCCESS) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: problem setting nonblocking  "  ) ;
			ap_remove_output_filter(f);
			 return ap_pass_brigade(f->next, bb) ;
		        }
		 
		       
		if ((status = apr_socket_timeout_set(sctx->sock, timeout)) != APR_SUCCESS) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory:setting socket timeout"  ) ;
			ap_remove_output_filter(f);
			 return ap_pass_brigade(f->next, bb) ;

		}
		
		if ((status = apr_socket_connect(sctx->sock, sockaddr)) != APR_SUCCESS) {
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory:problem connecting"  ) ;  
			ap_remove_output_filter(f);
			return ap_pass_brigade(f->next, bb) ;          
		}

	    
		char *buf;
		buf = apr_pstrcat(r->pool, "PUT " ,sctx-> puturl," HTTP/1.1",CRLF,"Host: ", conf->host, CRLF,"Connection: close",CRLF,"Transfer-Encoding: chunked",HEADEREND,NULL); 
                apr_size_t cb;	
        	cb = strlen(buf);
	     
		//status = apr_socket_send(sctx->sock,buf,&cb);

		//first chunk - client headers                                                                                                                                                                                                                                                                                                              
		  apr_off_t bytes = 0;
		  char chunk_hdr[20];
		  char *buf2;

		  buf2 = apr_pstrcat(r->pool, sctx->headers,HEADEREND,NULL);
		  bytes = strlen(buf2);

		  apr_size_t hdr_len;

		  hdr_len = apr_snprintf(chunk_hdr, sizeof(chunk_hdr),
					 "%" APR_UINT64_T_HEX_FMT CRLF, (apr_uint64_t)bytes);
		  ap_xlate_proto_to_ascii(chunk_hdr, hdr_len);

		  
		      char *buf3;
                      buf3 = apr_pstrcat(r->pool,buf,chunk_hdr,buf2,CRLF,NULL);
		      ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: fisrst chunk:%s", buf3);
                      cb = strlen (buf3);

		                  
		      apr_size_t rr =0; 
                      apr_size_t sent = 0;  
                        do {  
                           rr = cb - sent; 
			   status = apr_socket_send(sctx->sock,buf3 + sent ,&rr); 
			   sent += rr;      
			   //ap_log_rerror(APLOG_MARK, APLOG_DEBUG, status, r,"mod_sitestory: bytes send header %d", sent ) ; 
                           } while (status != APR_TIMEUP && rr!=0 && sent < cb  ); 
                          if ((rr == 0 && sent < cb) || status == APR_TIMEUP||status!=APR_SUCCESS) { 
                           ap_log_rerror(APLOG_MARK, APLOG_DEBUG, status, r ,"mod_sitestory: Connection timed out first attempt" ) ;
			   ap_remove_output_filter(f);
                          return ap_pass_brigade(f->next, bb) ;  
			  }   
			                        
			  
	}
	
	          
		for (b = APR_BRIGADE_FIRST(bb); b != APR_BRIGADE_SENTINEL(bb); b = APR_BUCKET_NEXT(b)) {
             		  
			ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
						 "mod_sitestory:  %s (%s-%s): %" APR_SIZE_T_FMT " bytes",
						 f->frec->name,
						 (APR_BUCKET_IS_METADATA(b)) ? "metadata" : "data",
						 b->type->name,
						 b->length) ;
			
		     
			//apr_size_t bsize =  b->length;

                 if (APR_BUCKET_IS_EOS(b)) {
		   apr_size_t cb;	
                    char *endchunk;
		    apr_status_t rv;
                    endchunk = apr_pstrcat(r->pool, ASCII_ZERO , CRLF,CRLF,NULL);
                    cb =strlen(endchunk);
		     rv = apr_socket_send(sctx->sock,endchunk,&cb);
		     char errmsg[256];
                     if (rv!=APR_SUCCESS) {
		       ap_log_rerror(APLOG_MARK, APLOG_DEBUG, rv, r,"mod_sitestory EOS: Connection timed out %s ",apr_strerror(rv, errmsg, sizeof errmsg)) ;
		       
		       apr_socket_close( sctx->sock );    
		      ap_remove_output_filter(f);
		      return ap_pass_brigade(f->next, bb) ;
		    }

		     apr_socket_close( sctx->sock );
			break;
                    }
		 else {
					
		 if (!(APR_BUCKET_IS_METADATA(b))) {
		   const	char *buf = 0;
			apr_size_t nbytes;
			apr_size_t cb;
			char errmsg[256];
			//int counter=0;
		      
                    	apr_status_t rv;
                                   
			
			if (apr_bucket_read(b, &buf, &nbytes, APR_BLOCK_READ) == APR_SUCCESS) {
			
                        	  			   
			          if  (nbytes) {			
			            char chunk_hdr[20];
			            apr_size_t hdr_len;
			     
                                ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: length from nbytes:%" APR_SIZE_T_FMT , nbytes);   
			        hdr_len = apr_snprintf(chunk_hdr, sizeof(chunk_hdr),
						    "%" APR_UINT64_T_HEX_FMT CRLF, (apr_uint64_t)nbytes);
			        ap_xlate_proto_to_ascii(chunk_hdr, hdr_len);
			        status = apr_socket_send(sctx->sock,chunk_hdr,&hdr_len);
                              if ( status != APR_SUCCESS){
				ap_log_rerror(APLOG_MARK, APLOG_DEBUG, status, r,"mod_sitestory meta header section: Connection timed out" ) ;
				ap_remove_output_filter(f);
				return ap_pass_brigade(f->next, bb) ;
			      }
			      //ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: next chunk header:%s", chunk_hdr);  
			          apr_size_t rr = 0; 
			          apr_size_t sent = 0;  
				  do { rr = nbytes - sent; 
				    status = apr_socket_send(sctx->sock,buf + sent ,&rr);
				    sent += rr; 
				    //  ap_log_rerror(APLOG_MARK, APLOG_DEBUG, status, r,"mod_sitestory data section: bytes send %d",sent);                                    
                                   } while (status != APR_TIMEUP && rr!=0 && sent < nbytes  ); 
				  if((rr == 0 && sent < nbytes) || status == APR_TIMEUP) {
                                   ap_log_rerror(APLOG_MARK, APLOG_DEBUG, status, r,"mod_sitestory data section: Connection timed out %s",apr_strerror(status, errmsg, sizeof errmsg)) ;
			            ap_remove_output_filter(f); 
				    return ap_pass_brigade(f->next, bb); 
				  }
                                 // ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: next chunk header:%s", buf); 
                                 //sending end of chunk
				  
			        cb = strlen(ASCII_CRLF);
			        //apr_status_t rv;
				rv = apr_socket_send(sctx->sock,ASCII_CRLF,&cb);
			         if (rv!= APR_SUCCESS){
			        ap_log_rerror(APLOG_MARK, APLOG_DEBUG, rv, r,"mod_sitestory end of chunk: Connection timed out %s",apr_strerror(rv, errmsg, sizeof errmsg) ) ;
			        ap_remove_output_filter(f);
			        return ap_pass_brigade(f->next, bb) ;
			        }
				  
				 
			        } //nbytes>0                            	
				 
			}//apr_success
			 else {
			  ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,  "mod_sitestory:  %s (%s-%s): %s",  f->frec->name,
				       (APR_BUCKET_IS_METADATA(b)) ? "metadata" : "data",  b->type->name, "error reading data"); 
			   }
		   
     
		 }
		 }
		
      
		}
		
		 return ap_pass_brigade(f->next, bb) ;
	}



          static const char* set_dirs(cmd_parms* cmd, void* CFG, const char* arg) {
	      ta_cfg* cfg = CFG;
	      if (cfg->excluded_dirs == NULL) {
              cfg->excluded_dirs = apr_array_make(cmd->pool, 20, sizeof(const char*));
              }
              *(const char**)apr_array_push(cfg->excluded_dirs) = arg;
              
            return NULL ;
              }


                               
     static int check_excluded_dirs (void*    arg,request_rec *r ){
          ta_cfg*    conf  =(ta_cfg *) arg;
          //char *path= r->parsed_uri.path;
	  //char *name= r->filename;
	  char *uri =  r->unparsed_uri;
	  char *good;
	  int i;
	  int filoc;
	  int ex = 0;

          if (conf->excluded_dirs==NULL) return 0;

	  filoc = ap_rind(uri, '/');

	  if (filoc == -1 || (strcmp(r->uri, "/") == 0)) {
	    return 0;
	  }
	  else {
            
	    good = apr_pstrdup(r->pool, uri);
            char *token;
            char * a;
            char * first;

            token = apr_strtok(good,"/", &a); 
	    first = apr_pstrcat(r->pool,"/",token,NULL);
            ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory:  dir excluded  request in if good  %s",first) ;

	    for (i = 0; i < conf->excluded_dirs->nelts; i++) {
	      const char *s = ((const char**)conf->excluded_dirs->elts)[i];
	      if ( strcmp(first, s) ==0) {
		ex = 1;
                return ex;
	      }
	      ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: dir excluded  request in if  %s",s) ;
	      //   ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory:  hook  request in if  %s",uri) ;
	    }

	  }

	  return ex;
        }

	static int myta_pre_conn(request_rec *r)
       {    srv = ap_get_server_name(r);
	    char *line;
            char *req_line;
            char *protocolstr;
	    conf = ap_get_module_config(r->per_dir_config, &sitestory_module);
	    // my_svr_cfg*    srv_conf = ap_get_module_config(r->server->module_config,&sitestory_module); 
	    // apr_status_t rv;
	    const char *dt =apr_table_get(r->headers_in,"Accept-Datetime");
	    req_line  = apr_pstrdup(r->pool, r->the_request);
	    char *a;
	    char *token;
            char *sprotocol;
            protocolstr = apr_pstrdup(r->pool,r->protocol);
	    char * pch;
            char * pcH;
	    pch=strchr(protocolstr,'s');
            pcH=strchr(protocolstr,'S');
            if (pch != NULL|| pcH!=NULL){
	      sprotocol = "https://";
            }
            else {
	      sprotocol = "http://";
	    }

	    token = apr_strtok(req_line," ", &a);
	    token = apr_strtok(NULL, " ", &a);   
	    line = apr_pstrcat(r->pool,"<",conf->tgurl,sprotocol,r->hostname,token,">;rel=timegate",NULL);
	    int ex = 0;
	    ex = check_excluded_dirs(conf,r);
            if (conf->tgurl!=NULL) {
	      if (ex==0) {
              apr_table_set(r->headers_out, "Link", line);
	      }
	    }
            //apr_table_add(r->headers_out, "Link", r->the_request);
	    if (conf->enable_ta) {
	      // if (conf->tgurl!=NULL) {
	      // apr_table_set(r->headers_out, "Link", line);
	      //}
	    if ( dt==NULL) {
	      //apr_table_set(r->headers_out, "Link", line);
	      //    if (conf->enable_ta) {
                // We only process GET  requests.  
		if(r->method_number == M_GET){ 
		  // int ex=0;		  
		  //  ex= check_excluded_dirs(conf,r);
		      ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory:  excluded  flag %d",ex) ;
		      if (ex==0) {
			 char *uri =  r->unparsed_uri;
			 ap_log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,"mod_sitestory: added mod_sitestory for request  %s",uri) ;
                         ap_add_output_filter("MOD_TA_OUT3", NULL, r, r->connection);
			   
		      }
		      
		}//mget
               
		} //enable
		   
	    }//dt
	        
		return OK;
	}

                  

	static void myta_register_hooks(apr_pool_t *p)
	{
		/*
		 * We know that SSL is CONNECTION + 5
		 */
		//ap_register_output_filter("MY_TA_OUT", ta_out_filter,ta_filter_init, AP_FTYPE_CONNECTION + 3) ;
		//ap_register_output_filter("MY_TA_OUT2", ta_out_filter2,NULL, AP_FTYPE_RESOURCE) ;
		//ap_register_output_filter("MY_TA_OUT2", ta_out_filter2,NULL, AP_FTYPE_CONTENT_SET) ;
		// ap_register_output_filter("MOD_TA_OUT3", ta_out_filter3,NULL, AP_FTYPE_CONNECTION - 1);
		ap_register_output_filter("MOD_TA_OUT3", ta_out_filter3,NULL, AP_FTYPE_TRANSCODE - 1);
		//ap_hook_fixups(myta_pre_conn, NULL, NULL, APR_HOOK_MIDDLE);
		//ap_hook_fixups(myta_pre_conn, NULL, NULL, APR_HOOK_FIRST);
		ap_hook_post_read_request(myta_pre_conn, NULL, NULL, APR_HOOK_FIRST);
		//ap_hook_child_init(my_child_init,NULL,NULL,APR_HOOK_MIDDLE);
		//ap_hook_post_config(my_hook_post_config, NULL, NULL, APR_HOOK_MIDDLE);
	}
           static const command_rec put_cmds[] = {
	     // AP_INIT_TAKE1("ArchiveHostTest",set_host,NULL,RSRC_CONF,"host to put response to archive"),
	     //  AP_INIT_ITERATE("Excluded",set_dirs,NULL,RSRC_CONF,"dirs  to exclude from  archive"),
            AP_INIT_FLAG("EnableArchiving",ap_set_flag_slot,(void*)APR_OFFSETOF(ta_cfg,enable_ta),OR_ALL,"enable archive plugin"),
	    AP_INIT_FLAG("EnableIP",ap_set_flag_slot,(void*)APR_OFFSETOF(ta_cfg,enable_ip),OR_ALL,"enable archiving ip"),
            AP_INIT_TAKE1("ArchivePath",ap_set_string_slot,(void*)APR_OFFSETOF(ta_cfg, archiveurl),OR_ALL,"url to put response to archive"),
            AP_INIT_TAKE1("ArchiveHost",ap_set_string_slot,(void*)APR_OFFSETOF(ta_cfg, host),OR_ALL,"host to put response to archive"),
            AP_INIT_TAKE1("ArchivePort",ap_set_string_slot,(void*)APR_OFFSETOF(ta_cfg, port),OR_ALL,"port to put response to archive"),
	    AP_INIT_TAKE1("ArchiveTimeGate",ap_set_string_slot,(void*)APR_OFFSETOF(ta_cfg, tgurl),OR_ALL,"url to add to link header"),
	    AP_INIT_ITERATE("Excluded", set_dirs, NULL, OR_ALL, "Strings to be treated as list of dirs"),
             { NULL }
             };

	module AP_MODULE_DECLARE_DATA sitestory_module = {
		STANDARD20_MODULE_STUFF,
		put_config,      /* dir config creater */
		NULL,           /* dir merger --- default is to override */
		NULL,  /* server config */
		NULL,         /* merge server configs */
		put_cmds,
		myta_register_hooks  /* register hooks */
	};
