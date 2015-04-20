package parsers.cookie;

import network.http.HTTPCookie;
import parsers.Parser;

public class CookieParser extends Parser {

	@Override
	/**
	 * @param content raw "Set-Cookie" header value of the cookie, i.e. it has the form
	 * MyCookieName=MyCookie|number|; path=/; expires= Fri 08-Jan-1999 13:00:00 GMT
	 */
	public HTTPCookie parse(String content) {
		
        String cookieBase = content.substring(0, content.indexOf(";"));
        String cookieName = cookieBase.substring(0, cookieBase.indexOf("="));
        String cookieValue = cookieBase.substring(cookieBase.indexOf("=") + 1, cookieBase.length());
		
		HTTPCookie httpCookie = new HTTPCookie();
		httpCookie.setName(cookieName);
		httpCookie.setValue(cookieValue);

		return httpCookie;
	}

}
