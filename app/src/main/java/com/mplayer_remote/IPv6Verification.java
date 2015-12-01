/*
MPlayer Remote
    Copyright (C) 2015  Rafał Kałęcki

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.mplayer_remote;

import java.util.regex.Pattern;

/**
 * Klasa pomocnicza pozwalającą weryfikować składnię tekstowej reprezentacji adresu IP w wersji 6 (szóstej).
 * @author sokar
 *
 */
public class IPv6Verification {
		
	/**
	 * Wyrażenie regularne pozwalające weryfikować czy dany ciąg znaków jest tekstową reprezentacją adresu IP w wersji szóstej. 
	 */
	private static final String REGEX = "^(((?=(?>.*?::)(?!.*::)))(::)?([0-9A-F]{1,4}::?){0,5}|([0-9A-F]{1,4}:){6})(\\2([0-9A-F]{1,4}(::?|$)){0,2}|((25[0-5]|(2[0-4]|1\\d|[1-9])?\\d)(\\.|$)){4}|[0-9A-F]{1,4}:[0-9A-F]{1,4})(?<![^:]:|\\.)\\z";
	
	/**
	 * Skompilowane wyrażenie regularne {@link com.mplayer_remote.IPv6Verification#REGEX}.
	 * @see java.util.regex.Pattern 
	 */
	private static final Pattern IPV6_PATTERN = Pattern.compile(REGEX,Pattern.CASE_INSENSITIVE);
		
    /**
     * Sprawdza czy parametr <code>input</code> jest składniowo poprawną tekstową reprezentacją adresu IP w wersji 6.  
     * @param input tekstowa reprezentacja adresu IP w wersji 6.
     * @return
     */
    public static boolean isIPv6Address(final String input) {
        return IPV6_PATTERN.matcher(input).matches();
    }
}
