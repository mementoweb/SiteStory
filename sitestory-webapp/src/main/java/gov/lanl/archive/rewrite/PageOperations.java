/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package gov.lanl.archive.rewrite;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 *
 *
 * @author Lyudmila Balakireva
 * @version 
 */
public class PageOperations implements LinkConverter {
	/**
	 * configuration name for URL prefix of replay server
	 */
	private String replayURIPrefix = null;
	private String aggregatorURIPrefix = null;
	private String pageURI = null;
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		StringBuilder sb = null;

		if(replayURIPrefix == null) {
			sb = new StringBuilder(url.length() + datespec.length());
			sb.append(datespec);
			sb.append("/");
			sb.append(UrlOperations.stripDefaultPortFromUrl(url));
			return sb.toString();
		}
		if(url.startsWith(replayURIPrefix)) {
			return url;
		}
		//if (aggregatorURIPrefix!=null) {
		    //not sure about aggregator timegate 
			//but at least not rewrite external urls for now.
			String host = UrlOperations.urlToHost(url);
			String mhost =  UrlOperations.urlToHost(pageURI);
			
			
			 if (!host.equals(mhost)) {
				 if (aggregatorURIPrefix!=null) {
					 sb = new StringBuilder(url.length() + datespec.length());
					    sb.append(aggregatorURIPrefix);
						sb.append(datespec);
						sb.append("/");
						sb.append(UrlOperations.stripDefaultPortFromUrl(url));
						return sb.toString();
				 }
				 else {
				  return url;
				 }
			}
		//}
		sb = new StringBuilder(url.length() + datespec.length());
		sb.append(replayURIPrefix);
		sb.append(datespec);
		sb.append("/");
		sb.append(UrlOperations.stripDefaultPortFromUrl(url));
		return sb.toString();
	}

	/**
	 * @param replayURIPrefix the replayURIPrefix to set
	 */
	public void setReplayURIPrefix(String replayURIPrefix) {
		this.replayURIPrefix = replayURIPrefix;
	}

	/**
	 * @param aggregatorURIPrefix the aggregatorURIPrefix to set
	 */
	public void setAggregatorURIPrefix(String aggregatorURIPrefix) {
		this.aggregatorURIPrefix = aggregatorURIPrefix;
	}
		
	public void setPageURI(String pageURI) {
		this.pageURI = pageURI;
	}
	
	/**
	 * @return the replayURIPrefix
	 */
	public String getReplayURIPrefix() {
		return replayURIPrefix;
	}
	/**
	 * @return the aggregatorURIPrefix
	 */
	public String getAggregatorURIPrefix() {
		return aggregatorURIPrefix;
	}
	
	public String getPageURI() {
		return pageURI;
	}
}
