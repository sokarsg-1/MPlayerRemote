package com.mplayer_remote.test;

import com.mplayer_remote.ServerList;

import android.test.AndroidTestCase;

public class ServerListUnitTest extends AndroidTestCase{

	public ServerListUnitTest() {
		// TODO Auto-generated constructor stub
	}
	
	private void ipv6test(boolean goodBadIPv6, String IPv6testCaseString) throws Exception{
		if (goodBadIPv6 == true){
			assertTrue(ServerList.isIPv4OrIPv6(IPv6testCaseString));
		}else{
			assertFalse(ServerList.isIPv4OrIPv6(IPv6testCaseString));
		}
		
	}
	
	private void ipv4test(boolean goodBadIPv4, String IPv4testCaseString){
		if (goodBadIPv4 == true){
			assertTrue(ServerList.isIPv4OrIPv6(IPv4testCaseString));
		}else{
			assertFalse(ServerList.isIPv4OrIPv6(IPv4testCaseString));
		}
	}
	
	public void testIPv4TestCases(){
		//The test cases...
		//ipv4test(true, "255.255.255.255");
		//ipv4test(false, "256.255.255.255");
		//ipv4test(false, "-1.255.255.255");
		
		ipv4test(true, "111.111.111.111");
		ipv4test(true, "255.255.255.255");
		ipv4test(true, "0.0.0.0");
		ipv4test(false,"256.111.111.111");
		ipv4test(false,"-1.111.111.111");
		ipv4test(true, "255.111.111.111");
		ipv4test(true, "0.111.111.111");
		ipv4test(false,"111.256.111.111");
		ipv4test(false,"111.-1.111.111");
		ipv4test(true, "111.255.111.111");
		ipv4test(true, "111.0.111.111");
		ipv4test(false,"111.111.256.111");
		ipv4test(false,"111.111.-1.111");
		ipv4test(true, "111.111.255.111");
		ipv4test(true, "111.111.0.111");
		ipv4test(false,"111.111.111.256");
		ipv4test(false,"111.111.111.-1");
		ipv4test(true, "111.111.111.255");
		ipv4test(true, "111.111.111.0");
		
		ipv4test(false,"111.111.111");
		ipv4test(false,"111.111.111.111.111");
		
		//leading zeros allowed not recommended
		ipv4test(true, "011.111.111.111");
		ipv4test(true, "001.111.111.111");
		ipv4test(true, "111.011.111.111");
		ipv4test(true, "111.001.111.111");
		ipv4test(true, "111.111.011.111");
		ipv4test(true, "111.111.001.111");
		ipv4test(true, "111.111.111.011");
		ipv4test(true, "111.111.111.001");
	}
	
	public void testIPv6TestCases() throws Exception{
		// The test cases...

		ipv6test(false,"");// empty string 
		ipv6test(true,"::1");// loopback, compressed, non-routable 
		ipv6test(true,"::");// unspecified, compressed, non-routable 
		ipv6test(true,"0:0:0:0:0:0:0:1");// loopback, full 
		ipv6test(true,"0:0:0:0:0:0:0:0");// unspecified, full 
		ipv6test(true,"2001:DB8:0:0:8:800:200C:417A");// unicast, full 
		ipv6test(true,"FF01:0:0:0:0:0:0:101");// multicast, full 
		ipv6test(true,"2001:DB8::8:800:200C:417A");// unicast, compressed 
		ipv6test(true,"FF01::101");// multicast, compressed 
		ipv6test(false,"2001:DB8:0:0:8:800:200C:417A:221");// unicast, full 
		ipv6test(false,"FF01::101::2");// multicast, compressed 
		ipv6test(true,"fe80::217:f2ff:fe07:ed62");

		ipv6test(true,"2001:0000:1234:0000:0000:C1C0:ABCD:0876");
		ipv6test(true,"3ffe:0b00:0000:0000:0001:0000:0000:000a");
		ipv6test(true,"FF02:0000:0000:0000:0000:0000:0000:0001");
		ipv6test(true,"0000:0000:0000:0000:0000:0000:0000:0001");
		ipv6test(true,"0000:0000:0000:0000:0000:0000:0000:0000");
		ipv6test(false,"02001:0000:1234:0000:0000:C1C0:ABCD:0876");	// extra 0 not allowed! 
		ipv6test(false,"2001:0000:1234:0000:00001:C1C0:ABCD:0876");	// extra 0 not allowed! 
		//ipv6test(true," 2001:0000:1234:0000:0000:C1C0:ABCD:0876");		// leading space
		//ipv6test(true,"2001:0000:1234:0000:0000:C1C0:ABCD:0876");		// trailing space
		//ipv6test(true," 2001:0000:1234:0000:0000:C1C0:ABCD:0876  ");	// leading and trailing space
		ipv6test(false,"2001:0000:1234:0000:0000:C1C0:ABCD:0876  0");	// junk after valid address
		ipv6test(false,"2001:0000:1234: 0000:0000:C1C0:ABCD:0876");	// internal space

		ipv6test(false,"3ffe:0b00:0000:0001:0000:0000:000a");			// seven segments
		ipv6test(false,"FF02:0000:0000:0000:0000:0000:0000:0000:0001");	// nine segments
		ipv6test(false,"3ffe:b00::1::a");								// double "::"
		ipv6test(false,"::1111:2222:3333:4444:5555:6666::");			// double "::"
		ipv6test(true,"2::10");
		ipv6test(true,"ff02::1");
		ipv6test(true,"fe80::");
		ipv6test(true,"2002::");
		ipv6test(true,"2001:db8::");
		ipv6test(true,"2001:0db8:1234::");
		ipv6test(true,"::ffff:0:0");
		ipv6test(true,"::1");
		ipv6test(true,"1:2:3:4:5:6:7:8");
		ipv6test(true,"1:2:3:4:5:6::8");
		ipv6test(true,"1:2:3:4:5::8");
		ipv6test(true,"1:2:3:4::8");
		ipv6test(true,"1:2:3::8");
		ipv6test(true,"1:2::8");
		ipv6test(true,"1::8");
		ipv6test(true,"1::2:3:4:5:6:7");
		ipv6test(true,"1::2:3:4:5:6");
		ipv6test(true,"1::2:3:4:5");
		ipv6test(true,"1::2:3:4");
		ipv6test(true,"1::2:3");
		ipv6test(true,"1::8");
		ipv6test(true,"::2:3:4:5:6:7:8");
		ipv6test(true,"::2:3:4:5:6:7");
		ipv6test(true,"::2:3:4:5:6");
		ipv6test(true,"::2:3:4:5");
		ipv6test(true,"::2:3:4");
		ipv6test(true,"::2:3");
		ipv6test(true,"::8");
		ipv6test(true,"1:2:3:4:5:6::");
		ipv6test(true,"1:2:3:4:5::");
		ipv6test(true,"1:2:3:4::");
		ipv6test(true,"1:2:3::");
		ipv6test(true,"1:2::");
		ipv6test(true,"1::");
		ipv6test(true,"1:2:3:4:5::7:8");
		ipv6test(false,"1:2:3::4:5::7:8");							// Double "::"
		ipv6test(false,"12345::6:7:8");
		ipv6test(true,"1:2:3:4::7:8");
		ipv6test(true,"1:2:3::7:8");
		ipv6test(true,"1:2::7:8");
		ipv6test(true,"1::7:8");

		// IPv4 addresses as dotted-quads
		ipv6test(true,"1:2:3:4:5:6:1.2.3.4");
		ipv6test(true,"1:2:3:4:5::1.2.3.4");
		ipv6test(true,"1:2:3:4::1.2.3.4");
		ipv6test(true,"1:2:3::1.2.3.4");
		ipv6test(true,"1:2::1.2.3.4");
		ipv6test(true,"1::1.2.3.4");
		ipv6test(true,"1:2:3:4::5:1.2.3.4");
		ipv6test(true,"1:2:3::5:1.2.3.4");
		ipv6test(true,"1:2::5:1.2.3.4");
		ipv6test(true,"1::5:1.2.3.4");
		ipv6test(true,"1::5:11.22.33.44");
		ipv6test(false,"1::5:400.2.3.4");
		ipv6test(false,"1::5:260.2.3.4");
		ipv6test(false,"1::5:256.2.3.4");
		ipv6test(false,"1::5:1.256.3.4");
		ipv6test(false,"1::5:1.2.256.4");
		ipv6test(false,"1::5:1.2.3.256");
		ipv6test(false,"1::5:300.2.3.4");
		ipv6test(false,"1::5:1.300.3.4");
		ipv6test(false,"1::5:1.2.300.4");
		ipv6test(false,"1::5:1.2.3.300");
		ipv6test(false,"1::5:900.2.3.4");
		ipv6test(false,"1::5:1.900.3.4");
		ipv6test(false,"1::5:1.2.900.4");
		ipv6test(false,"1::5:1.2.3.900");
		ipv6test(false,"1::5:300.300.300.300");
		ipv6test(false,"1::5:3000.30.30.30");
		ipv6test(false,"1::400.2.3.4");
		ipv6test(false,"1::260.2.3.4");
		ipv6test(false,"1::256.2.3.4");
		ipv6test(false,"1::1.256.3.4");
		ipv6test(false,"1::1.2.256.4");
		ipv6test(false,"1::1.2.3.256");
		ipv6test(false,"1::300.2.3.4");
		ipv6test(false,"1::1.300.3.4");
		ipv6test(false,"1::1.2.300.4");
		ipv6test(false,"1::1.2.3.300");
		ipv6test(false,"1::900.2.3.4");
		ipv6test(false,"1::1.900.3.4");
		ipv6test(false,"1::1.2.900.4");
		ipv6test(false,"1::1.2.3.900");
		ipv6test(false,"1::300.300.300.300");
		ipv6test(false,"1::3000.30.30.30");
		ipv6test(false,"::400.2.3.4");
		ipv6test(false,"::260.2.3.4");
		ipv6test(false,"::256.2.3.4");
		ipv6test(false,"::1.256.3.4");
		ipv6test(false,"::1.2.256.4");
		ipv6test(false,"::1.2.3.256");
		ipv6test(false,"::300.2.3.4");
		ipv6test(false,"::1.300.3.4");
		ipv6test(false,"::1.2.300.4");
		ipv6test(false,"::1.2.3.300");
		ipv6test(false,"::900.2.3.4");
		ipv6test(false,"::1.900.3.4");
		ipv6test(false,"::1.2.900.4");
		ipv6test(false,"::1.2.3.900");
		ipv6test(false,"::300.300.300.300");
		ipv6test(false,"::3000.30.30.30");
		ipv6test(true,"fe80::217:f2ff:254.7.237.98");
		ipv6test(true,"::ffff:192.168.1.26");
		ipv6test(false,"2001:1:1:1:1:1:255Z255X255Y255");				// garbage instead of "." in IPv4
		ipv6test(false,"::ffff:192x168.1.26");							// ditto
		ipv6test(true,"::ffff:192.168.1.1");
		ipv6test(true,"0:0:0:0:0:0:13.1.68.3");// IPv4-compatible IPv6 address, full, deprecated 
		ipv6test(true,"0:0:0:0:0:FFFF:129.144.52.38");// IPv4-mapped IPv6 address, full 
		ipv6test(true,"::13.1.68.3");// IPv4-compatible IPv6 address, compressed, deprecated 
		ipv6test(true,"::FFFF:129.144.52.38");// IPv4-mapped IPv6 address, compressed 
		ipv6test(true,"fe80:0:0:0:204:61ff:254.157.241.86");
		ipv6test(true,"fe80::204:61ff:254.157.241.86");
		ipv6test(true,"::ffff:12.34.56.78");
		ipv6test(false,"::ffff:2.3.4");
		ipv6test(false,"::ffff:257.1.2.3");
//		ipv6test(false,"1.2.3.4");

		ipv6test(false,"1.2.3.4:1111:2222:3333:4444::5555");  // Aeron
		ipv6test(false,"1.2.3.4:1111:2222:3333::5555");
		ipv6test(false,"1.2.3.4:1111:2222::5555");
		ipv6test(false,"1.2.3.4:1111::5555");
		ipv6test(false,"1.2.3.4::5555");
		ipv6test(false,"1.2.3.4::");

		// Testing IPv4 addresses represented as dotted-quads
		// Leading zero's in IPv4 addresses not allowed: some systems treat the leading "0" in ".086" as the start of an octal number
		// Update: The BNF in RFC-3986 explicitly defines the dec-octet (for IPv4 addresses) not to have a leading zero
		ipv6test(false,"fe80:0000:0000:0000:0204:61ff:254.157.241.086");
		ipv6test(true,"::ffff:192.0.2.128");   // but this is OK, since there's a single digit
		ipv6test(false,"XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:00.00.00.00");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:000.000.000.000");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:256.256.256.256");

		// Not testing address with subnet mask
		// ipv6test(true,"2001:0DB8:0000:CD30:0000:0000:0000:0000/60");// full, with prefix 
		// ipv6test(true,"2001:0DB8::CD30:0:0:0:0/60");// compressed, with prefix 
		// ipv6test(true,"2001:0DB8:0:CD30::/60");// compressed, with prefix //2 
		// ipv6test(true,"::/128");// compressed, unspecified address type, non-routable 
		// ipv6test(true,"::1/128");// compressed, loopback address type, non-routable 
		// ipv6test(true,"FF00::/8");// compressed, multicast address type 
		// ipv6test(true,"FE80::/10");// compressed, link-local unicast, non-routable 
		// ipv6test(true,"FEC0::/10");// compressed, site-local unicast, deprecated 
		// ipv6test(false,"124.15.6.89/60");// standard IPv4, prefix not allowed 

		ipv6test(true,"fe80:0000:0000:0000:0204:61ff:fe9d:f156");
		ipv6test(true,"fe80:0:0:0:204:61ff:fe9d:f156");
		ipv6test(true,"fe80::204:61ff:fe9d:f156");
		ipv6test(true,"::1");
		ipv6test(true,"fe80::");
		ipv6test(true,"fe80::1");
		ipv6test(false,":");
		ipv6test(true,"::ffff:c000:280");

		// Aeron supplied these test cases
		ipv6test(false,"1111:2222:3333:4444::5555:");
		ipv6test(false,"1111:2222:3333::5555:");
		ipv6test(false,"1111:2222::5555:");
		ipv6test(false,"1111::5555:");
		ipv6test(false,"::5555:");
		ipv6test(false,":::");
		ipv6test(false,"1111:");
		ipv6test(false,":");

		ipv6test(false,":1111:2222:3333:4444::5555");
		ipv6test(false,":1111:2222:3333::5555");
		ipv6test(false,":1111:2222::5555");
		ipv6test(false,":1111::5555");
		ipv6test(false,":::5555");
		ipv6test(false,":::");


		// Additional test cases
		// from http://rt.cpan.org/Public/Bug/Display.html?id=50693

		ipv6test(true,"2001:0db8:85a3:0000:0000:8a2e:0370:7334");
		ipv6test(true,"2001:db8:85a3:0:0:8a2e:370:7334");
		ipv6test(true,"2001:db8:85a3::8a2e:370:7334");
		ipv6test(true,"2001:0db8:0000:0000:0000:0000:1428:57ab");
		ipv6test(true,"2001:0db8:0000:0000:0000::1428:57ab");
		ipv6test(true,"2001:0db8:0:0:0:0:1428:57ab");
		ipv6test(true,"2001:0db8:0:0::1428:57ab");
		ipv6test(true,"2001:0db8::1428:57ab");
		ipv6test(true,"2001:db8::1428:57ab");
		ipv6test(true,"0000:0000:0000:0000:0000:0000:0000:0001");
		ipv6test(true,"::1");
		ipv6test(true,"::ffff:0c22:384e");
		ipv6test(true,"2001:0db8:1234:0000:0000:0000:0000:0000");
		ipv6test(true,"2001:0db8:1234:ffff:ffff:ffff:ffff:ffff");
		ipv6test(true,"2001:db8:a::123");
		ipv6test(true,"fe80::");

		ipv6test(false,"123");
		ipv6test(false,"ldkfj");
		ipv6test(false,"2001::FFD3::57ab");
		ipv6test(false,"2001:db8:85a3::8a2e:37023:7334");
		ipv6test(false,"2001:db8:85a3::8a2e:370k:7334");
		ipv6test(false,"1:2:3:4:5:6:7:8:9");
		ipv6test(false,"1::2::3");
		ipv6test(false,"1:::3:4:5");
		ipv6test(false,"1:2:3::4:5:6:7:8:9");

		// New from Aeron 
		ipv6test(true,"1111:2222:3333:4444:5555:6666:7777:8888");
		ipv6test(true,"1111:2222:3333:4444:5555:6666:7777::");
		ipv6test(true,"1111:2222:3333:4444:5555:6666::");
		ipv6test(true,"1111:2222:3333:4444:5555::");
		ipv6test(true,"1111:2222:3333:4444::");
		ipv6test(true,"1111:2222:3333::");
		ipv6test(true,"1111:2222::");
		ipv6test(true,"1111::");
		// ipv6test(true,"::");     //duplicate
		ipv6test(true,"1111:2222:3333:4444:5555:6666::8888");
		ipv6test(true,"1111:2222:3333:4444:5555::8888");
		ipv6test(true,"1111:2222:3333:4444::8888");
		ipv6test(true,"1111:2222:3333::8888");
		ipv6test(true,"1111:2222::8888");
		ipv6test(true,"1111::8888");
		ipv6test(true,"::8888");
		ipv6test(true,"1111:2222:3333:4444:5555::7777:8888");
		ipv6test(true,"1111:2222:3333:4444::7777:8888");
		ipv6test(true,"1111:2222:3333::7777:8888");
		ipv6test(true,"1111:2222::7777:8888");
		ipv6test(true,"1111::7777:8888");
		ipv6test(true,"::7777:8888");
		ipv6test(true,"1111:2222:3333:4444::6666:7777:8888");
		ipv6test(true,"1111:2222:3333::6666:7777:8888");
		ipv6test(true,"1111:2222::6666:7777:8888");
		ipv6test(true,"1111::6666:7777:8888");
		ipv6test(true,"::6666:7777:8888");
		ipv6test(true,"1111:2222:3333::5555:6666:7777:8888");
		ipv6test(true,"1111:2222::5555:6666:7777:8888");
		ipv6test(true,"1111::5555:6666:7777:8888");
		ipv6test(true,"::5555:6666:7777:8888");
		ipv6test(true,"1111:2222::4444:5555:6666:7777:8888");
		ipv6test(true,"1111::4444:5555:6666:7777:8888");
		ipv6test(true,"::4444:5555:6666:7777:8888");
		ipv6test(true,"1111::3333:4444:5555:6666:7777:8888");
		ipv6test(true,"::3333:4444:5555:6666:7777:8888");
		ipv6test(true,"::2222:3333:4444:5555:6666:7777:8888");
		ipv6test(true,"1111:2222:3333:4444:5555:6666:123.123.123.123");
		ipv6test(true,"1111:2222:3333:4444:5555::123.123.123.123");
		ipv6test(true,"1111:2222:3333:4444::123.123.123.123");
		ipv6test(true,"1111:2222:3333::123.123.123.123");
		ipv6test(true,"1111:2222::123.123.123.123");
		ipv6test(true,"1111::123.123.123.123");
		ipv6test(true,"::123.123.123.123");
		ipv6test(true,"1111:2222:3333:4444::6666:123.123.123.123");
		ipv6test(true,"1111:2222:3333::6666:123.123.123.123");
		ipv6test(true,"1111:2222::6666:123.123.123.123");
		ipv6test(true,"1111::6666:123.123.123.123");
		ipv6test(true,"::6666:123.123.123.123");
		ipv6test(true,"1111:2222:3333::5555:6666:123.123.123.123");
		ipv6test(true,"1111:2222::5555:6666:123.123.123.123");
		ipv6test(true,"1111::5555:6666:123.123.123.123");
		ipv6test(true,"::5555:6666:123.123.123.123");
		ipv6test(true,"1111:2222::4444:5555:6666:123.123.123.123");
		ipv6test(true,"1111::4444:5555:6666:123.123.123.123");
		ipv6test(true,"::4444:5555:6666:123.123.123.123");
		ipv6test(true,"1111::3333:4444:5555:6666:123.123.123.123");
		ipv6test(true,"::2222:3333:4444:5555:6666:123.123.123.123");

		// Playing with combinations of "0" and "::"
		// NB: these are all sytactically correct, but are bad form 
		//   because "0" adjacent to "::" should be combined into "::"
		ipv6test(true,"::0:0:0:0:0:0:0");
		ipv6test(true,"::0:0:0:0:0:0");
		ipv6test(true,"::0:0:0:0:0");
		ipv6test(true,"::0:0:0:0");
		ipv6test(true,"::0:0:0");
		ipv6test(true,"::0:0");
		ipv6test(true,"::0");
		ipv6test(true,"0:0:0:0:0:0:0::");
		ipv6test(true,"0:0:0:0:0:0::");
		ipv6test(true,"0:0:0:0:0::");
		ipv6test(true,"0:0:0:0::");
		ipv6test(true,"0:0:0::");
		ipv6test(true,"0:0::");
		ipv6test(true,"0::");

		// New invalid from Aeron
		// Invalid data
		ipv6test(false,"XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX");

		// Too many components
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777:8888:9999");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777:8888::");
		ipv6test(false,"::2222:3333:4444:5555:6666:7777:8888:9999");

		// Too few components
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777");
		ipv6test(false,"1111:2222:3333:4444:5555:6666");
		ipv6test(false,"1111:2222:3333:4444:5555");
		ipv6test(false,"1111:2222:3333:4444");
		ipv6test(false,"1111:2222:3333");
		ipv6test(false,"1111:2222");
		ipv6test(false,"1111");

		// Missing :
		ipv6test(false,"11112222:3333:4444:5555:6666:7777:8888");
		ipv6test(false,"1111:22223333:4444:5555:6666:7777:8888");
		ipv6test(false,"1111:2222:33334444:5555:6666:7777:8888");
		ipv6test(false,"1111:2222:3333:44445555:6666:7777:8888");
		ipv6test(false,"1111:2222:3333:4444:55556666:7777:8888");
		ipv6test(false,"1111:2222:3333:4444:5555:66667777:8888");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:77778888");

		// Missing : intended for ::
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777:8888:");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777:");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:");
		ipv6test(false,"1111:2222:3333:4444:5555:");
		ipv6test(false,"1111:2222:3333:4444:");
		ipv6test(false,"1111:2222:3333:");
		ipv6test(false,"1111:2222:");
		ipv6test(false,"1111:");
		ipv6test(false,":");
		ipv6test(false,":8888");
		ipv6test(false,":7777:8888");
		ipv6test(false,":6666:7777:8888");
		ipv6test(false,":5555:6666:7777:8888");
		ipv6test(false,":4444:5555:6666:7777:8888");
		ipv6test(false,":3333:4444:5555:6666:7777:8888");
		ipv6test(false,":2222:3333:4444:5555:6666:7777:8888");
		ipv6test(false,":1111:2222:3333:4444:5555:6666:7777:8888");

		// :::
		ipv6test(false,":::2222:3333:4444:5555:6666:7777:8888");
		ipv6test(false,"1111:::3333:4444:5555:6666:7777:8888");
		ipv6test(false,"1111:2222:::4444:5555:6666:7777:8888");
		ipv6test(false,"1111:2222:3333:::5555:6666:7777:8888");
		ipv6test(false,"1111:2222:3333:4444:::6666:7777:8888");
		ipv6test(false,"1111:2222:3333:4444:5555:::7777:8888");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:::8888");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777:::");

		// Double ::");
		ipv6test(false,"::2222::4444:5555:6666:7777:8888");
		ipv6test(false,"::2222:3333::5555:6666:7777:8888");
		ipv6test(false,"::2222:3333:4444::6666:7777:8888");
		ipv6test(false,"::2222:3333:4444:5555::7777:8888");
		ipv6test(false,"::2222:3333:4444:5555:7777::8888");
		ipv6test(false,"::2222:3333:4444:5555:7777:8888::");

		ipv6test(false,"1111::3333::5555:6666:7777:8888");
		ipv6test(false,"1111::3333:4444::6666:7777:8888");
		ipv6test(false,"1111::3333:4444:5555::7777:8888");
		ipv6test(false,"1111::3333:4444:5555:6666::8888");
		ipv6test(false,"1111::3333:4444:5555:6666:7777::");

		ipv6test(false,"1111:2222::4444::6666:7777:8888");
		ipv6test(false,"1111:2222::4444:5555::7777:8888");
		ipv6test(false,"1111:2222::4444:5555:6666::8888");
		ipv6test(false,"1111:2222::4444:5555:6666:7777::");

		ipv6test(false,"1111:2222:3333::5555::7777:8888");
		ipv6test(false,"1111:2222:3333::5555:6666::8888");
		ipv6test(false,"1111:2222:3333::5555:6666:7777::");

		ipv6test(false,"1111:2222:3333:4444::6666::8888");
		ipv6test(false,"1111:2222:3333:4444::6666:7777::");

		ipv6test(false,"1111:2222:3333:4444:5555::7777::");


		// Too many components"
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777:8888:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:5555:6666::1.2.3.4");
		ipv6test(false,"::2222:3333:4444:5555:6666:7777:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:1.2.3.4.5");

		// Too few components
		ipv6test(false,"1111:2222:3333:4444:5555:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:1.2.3.4");
		ipv6test(false,"1111:2222:3333:1.2.3.4");
		ipv6test(false,"1111:2222:1.2.3.4");
		ipv6test(false,"1111:1.2.3.4");
//		ipv6test(false,"1.2.3.4");

		// Missing :
		ipv6test(false,"11112222:3333:4444:5555:6666:1.2.3.4");
		ipv6test(false,"1111:22223333:4444:5555:6666:1.2.3.4");
		ipv6test(false,"1111:2222:33334444:5555:6666:1.2.3.4");
		ipv6test(false,"1111:2222:3333:44445555:6666:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:55556666:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:5555:66661.2.3.4");

		// Missing .
		ipv6test(false,"1111:2222:3333:4444:5555:6666:255255.255.255");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:255.255255.255");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:255.255.255255");

		// Missing : intended for ::
		ipv6test(false,":1.2.3.4");
		ipv6test(false,":6666:1.2.3.4");
		ipv6test(false,":5555:6666:1.2.3.4");
		ipv6test(false,":4444:5555:6666:1.2.3.4");
		ipv6test(false,":3333:4444:5555:6666:1.2.3.4");
		ipv6test(false,":2222:3333:4444:5555:6666:1.2.3.4");
		ipv6test(false,":1111:2222:3333:4444:5555:6666:1.2.3.4");

		// :::
		ipv6test(false,":::2222:3333:4444:5555:6666:1.2.3.4");
		ipv6test(false,"1111:::3333:4444:5555:6666:1.2.3.4");
		ipv6test(false,"1111:2222:::4444:5555:6666:1.2.3.4");
		ipv6test(false,"1111:2222:3333:::5555:6666:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:::6666:1.2.3.4");
		ipv6test(false,"1111:2222:3333:4444:5555:::1.2.3.4");

		// Double ::
		ipv6test(false,"::2222::4444:5555:6666:1.2.3.4");
		ipv6test(false,"::2222:3333::5555:6666:1.2.3.4");
		ipv6test(false,"::2222:3333:4444::6666:1.2.3.4");
		ipv6test(false,"::2222:3333:4444:5555::1.2.3.4");

		ipv6test(false,"1111::3333::5555:6666:1.2.3.4");
		ipv6test(false,"1111::3333:4444::6666:1.2.3.4");
		ipv6test(false,"1111::3333:4444:5555::1.2.3.4");

		ipv6test(false,"1111:2222::4444::6666:1.2.3.4");
		ipv6test(false,"1111:2222::4444:5555::1.2.3.4");

		ipv6test(false,"1111:2222:3333::5555::1.2.3.4");

		// Missing parts
		ipv6test(false,"::.");
		ipv6test(false,"::..");
		ipv6test(false,"::...");
		ipv6test(false,"::1...");
		ipv6test(false,"::1.2..");
		ipv6test(false,"::1.2.3.");
		ipv6test(false,"::.2..");
		ipv6test(false,"::.2.3.");
		ipv6test(false,"::.2.3.4");
		ipv6test(false,"::..3.");
		ipv6test(false,"::..3.4");
		ipv6test(false,"::...4");

		// Extra : in front
		ipv6test(false,":1111:2222:3333:4444:5555:6666:7777::");
		ipv6test(false,":1111:2222:3333:4444:5555:6666::");
		ipv6test(false,":1111:2222:3333:4444:5555::");
		ipv6test(false,":1111:2222:3333:4444::");
		ipv6test(false,":1111:2222:3333::");
		ipv6test(false,":1111:2222::");
		ipv6test(false,":1111::");
		ipv6test(false,":::");
		ipv6test(false,":1111:2222:3333:4444:5555:6666::8888");
		ipv6test(false,":1111:2222:3333:4444:5555::8888");
		ipv6test(false,":1111:2222:3333:4444::8888");
		ipv6test(false,":1111:2222:3333::8888");
		ipv6test(false,":1111:2222::8888");
		ipv6test(false,":1111::8888");
		ipv6test(false,":::8888");
		ipv6test(false,":1111:2222:3333:4444:5555::7777:8888");
		ipv6test(false,":1111:2222:3333:4444::7777:8888");
		ipv6test(false,":1111:2222:3333::7777:8888");
		ipv6test(false,":1111:2222::7777:8888");
		ipv6test(false,":1111::7777:8888");
		ipv6test(false,":::7777:8888");
		ipv6test(false,":1111:2222:3333:4444::6666:7777:8888");
		ipv6test(false,":1111:2222:3333::6666:7777:8888");
		ipv6test(false,":1111:2222::6666:7777:8888");
		ipv6test(false,":1111::6666:7777:8888");
		ipv6test(false,":::6666:7777:8888");
		ipv6test(false,":1111:2222:3333::5555:6666:7777:8888");
		ipv6test(false,":1111:2222::5555:6666:7777:8888");
		ipv6test(false,":1111::5555:6666:7777:8888");
		ipv6test(false,":::5555:6666:7777:8888");
		ipv6test(false,":1111:2222::4444:5555:6666:7777:8888");
		ipv6test(false,":1111::4444:5555:6666:7777:8888");
		ipv6test(false,":::4444:5555:6666:7777:8888");
		ipv6test(false,":1111::3333:4444:5555:6666:7777:8888");
		ipv6test(false,":::3333:4444:5555:6666:7777:8888");
		ipv6test(false,":::2222:3333:4444:5555:6666:7777:8888");
		ipv6test(false,":1111:2222:3333:4444:5555:6666:1.2.3.4");
		ipv6test(false,":1111:2222:3333:4444:5555::1.2.3.4");
		ipv6test(false,":1111:2222:3333:4444::1.2.3.4");
		ipv6test(false,":1111:2222:3333::1.2.3.4");
		ipv6test(false,":1111:2222::1.2.3.4");
		ipv6test(false,":1111::1.2.3.4");
		ipv6test(false,":::1.2.3.4");
		ipv6test(false,":1111:2222:3333:4444::6666:1.2.3.4");
		ipv6test(false,":1111:2222:3333::6666:1.2.3.4");
		ipv6test(false,":1111:2222::6666:1.2.3.4");
		ipv6test(false,":1111::6666:1.2.3.4");
		ipv6test(false,":::6666:1.2.3.4");
		ipv6test(false,":1111:2222:3333::5555:6666:1.2.3.4");
		ipv6test(false,":1111:2222::5555:6666:1.2.3.4");
		ipv6test(false,":1111::5555:6666:1.2.3.4");
		ipv6test(false,":::5555:6666:1.2.3.4");
		ipv6test(false,":1111:2222::4444:5555:6666:1.2.3.4");
		ipv6test(false,":1111::4444:5555:6666:1.2.3.4");
		ipv6test(false,":::4444:5555:6666:1.2.3.4");
		ipv6test(false,":1111::3333:4444:5555:6666:1.2.3.4");
		ipv6test(false,":::2222:3333:4444:5555:6666:1.2.3.4");

		// Extra : at end
		ipv6test(false,"1111:2222:3333:4444:5555:6666:7777:::");
		ipv6test(false,"1111:2222:3333:4444:5555:6666:::");
		ipv6test(false,"1111:2222:3333:4444:5555:::");
		ipv6test(false,"1111:2222:3333:4444:::");
		ipv6test(false,"1111:2222:3333:::");
		ipv6test(false,"1111:2222:::");
		ipv6test(false,"1111:::");
		ipv6test(false,":::");
		ipv6test(false,"1111:2222:3333:4444:5555:6666::8888:");
		ipv6test(false,"1111:2222:3333:4444:5555::8888:");
		ipv6test(false,"1111:2222:3333:4444::8888:");
		ipv6test(false,"1111:2222:3333::8888:");
		ipv6test(false,"1111:2222::8888:");
		ipv6test(false,"1111::8888:");
		ipv6test(false,"::8888:");
		ipv6test(false,"1111:2222:3333:4444:5555::7777:8888:");
		ipv6test(false,"1111:2222:3333:4444::7777:8888:");
		ipv6test(false,"1111:2222:3333::7777:8888:");
		ipv6test(false,"1111:2222::7777:8888:");
		ipv6test(false,"1111::7777:8888:");
		ipv6test(false,"::7777:8888:");
		ipv6test(false,"1111:2222:3333:4444::6666:7777:8888:");
		ipv6test(false,"1111:2222:3333::6666:7777:8888:");
		ipv6test(false,"1111:2222::6666:7777:8888:");
		ipv6test(false,"1111::6666:7777:8888:");
		ipv6test(false,"::6666:7777:8888:");
		ipv6test(false,"1111:2222:3333::5555:6666:7777:8888:");
		ipv6test(false,"1111:2222::5555:6666:7777:8888:");
		ipv6test(false,"1111::5555:6666:7777:8888:");
		ipv6test(false,"::5555:6666:7777:8888:");
		ipv6test(false,"1111:2222::4444:5555:6666:7777:8888:");
		ipv6test(false,"1111::4444:5555:6666:7777:8888:");
		ipv6test(false,"::4444:5555:6666:7777:8888:");
		ipv6test(false,"1111::3333:4444:5555:6666:7777:8888:");
		ipv6test(false,"::3333:4444:5555:6666:7777:8888:");
		ipv6test(false,"::2222:3333:4444:5555:6666:7777:8888:");

		// Additional cases: http://crisp.tweakblogs.net/blog/2031/ipv6-validation-%28and-caveats%29.html
		ipv6test(true,"0:a:b:c:d:e:f::");
		ipv6test(true,"::0:a:b:c:d:e:f"); // syntactically correct, but bad form (::0:... could be combined)
		ipv6test(true,"a:b:c:d:e:f:0::");
		ipv6test(false,"':10.0.0.1"); 




	}
}