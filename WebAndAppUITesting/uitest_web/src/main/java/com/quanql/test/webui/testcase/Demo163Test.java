package com.quanql.test.webui.testcase;

import org.testng.annotations.Test;

import com.quanql.test.core.utils.AssertUtil;
import com.quanql.test.webui.base.WebBaseTest;
import com.quanql.test.webui.page.DemoBaiduPage;

public class Demo163Test extends WebBaseTest {

	@Test
	public void testSearchNetease() {
		DemoBaiduPage.openBaidu();
		DemoBaiduPage.typeInSearchEdt("网易");
		DemoBaiduPage.clickSearchBtn();
		AssertUtil.assertTrue(DemoBaiduPage.isNeteaseExisted(), "网易没有出现在结果第1位！");
	}


	public static void main(String[] args) {
		System.out.println("测试是否能够运行！！");
	}
	
}
