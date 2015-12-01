package com.mplayer_remote.test;

import android.content.Context;
import android.test.AndroidTestCase;

public class XMLReaderWriterUnitTest extends AndroidTestCase {
	private Context mContext;
	
	public XMLReaderWriterUnitTest() {
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void setUp() throws Exception{
		super.setUp();
						
		mContext = getContext();
	}
	public void testPreConditions() {
		assertNotNull(mContext);
	}
	
}
