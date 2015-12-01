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

/**
 * Obiekt klasy <code>Server</code> reprezentuje serwer SSH, z którym aplikacja MPlayer Remote będzie się łączyć.
 * Obiekt <code>Server</code> przechowuje nazwę serwera SSH, jego adres IP, nazwę zarejestrowanego na serwerze użytkownika i hasło do jego konta.
 * 
 * @author sokar
 *
 */
public class Server{
		//w celach diagnostycznych nazwa logu dla tego Activity
	private static final String TAG = "Server";
	/**String zawierający nazwę serwera.*/ 
	private String serverNameString;
	/**String zawierający adres IP serwera.*/
	private String iPAaddressString;
	/**String zawierający nazwę konta użytkownika zarejestrowanego na serwerze.*/ 
	private String username;
	/**Tablica znaków zawierająca hasło do konta użytkownika zarejestrowanego na serwerze.*/
	private char[] password;
	
	/**
	 * Tworzy nowy pusty obiekt klasy Server.
	 */
	public Server(){
		serverNameString = "";
		iPAaddressString = "";
		username = "";
		password = "".toCharArray();
	}
	/**
	 * Ustawia nazwę serwera na wartość <code>serverNameString</code>.
	 * @param serverNameString nowa nazwa serwera.
	 */
	public void setServerName(String serverNameString) {
		this.serverNameString = serverNameString;
	}
	/**
	 * Zwraca wartość pola <code>serverNameString</code>.
	 * @return wartość pola <code>serverNameString</code>.
	 */
	public String getServerName() {
		return serverNameString;
	}
	/** 
	 * Ustawia adres IP serwera na wartość <code>iPAddress</code>.
	 * @param iPAddress nowy adres IP serwera.
	 */
	public void setIPAddress(String iPAddress) {
		this.iPAaddressString = iPAddress; 
	}
	/**
	 * Zwraca wartość pola <code>iPAddress</code>.
	 * @return wartość pola <code>iPAddress</code>.
	 */
	public String getIPAddress() {
		return iPAaddressString;
	}
	/**
	 * Ustawia nazwę zarejestrowanego na serwerze użytkownika na wartość <code>username</code>.
	 * @param username nowa nazwa zarejestrowanego na serwerze użytkownika.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * Zwraca wartość pola <code>username</code>.
	 * @return wartość pola <code>username</code>.
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * Ustawia hasło do konta użytkownika zarejestrowanego na serwerze na wartość <code>password</code>.
	 * @param password nowe hasło do konta użytkownika zarejestowanego na serwerze.
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}
	/**
	 * Zwraca wartość pola <code>password</code>.
	 * @return wartość pola <code>password</code>.
	 */
	public char[] getPassword() {
		return password;
	}
	
}

