package main;
import java.io.IOException;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class CustomConnection {
	 public static Connection.Response connect(String pc_username, String pc_password)throws IOException {
	        return Jsoup.connect(Constants.Url.PC_LOGIN).timeout(10 * 1000)
	                .userAgent(Constants.USER_AGENT_FIREFOX).data("__EVENTTARGET", "").data("__EVENTARGUMENT", "")
	                .data("__VIEWSTATE",
	                        "/wEPDwUJLTE5NTk1ODgyD2QWAmYPZBYCAgEPZBYEAgEPZBYCAgUPZBYEZg8WAh4EVGV4dAWQATwhLS1baWYgSUUgNl0+DQoJCTxsaW5rIHJlbD0nc3R5bGVzaGVldCcgdHlwZT0ndGV4dC9jc3MnIGhyZWY9J2h0dHA6Ly9zZWxmc2VydmljZS5hZGEuZWR1LmF6L1dlYi8vU2l0ZVJlc291cmNlcy9jc3MvZml4X2llNi5jc3MnPg0KCTwhW2VuZGlmXS0tPmQCAg8WAh8ABZABPCEtLVtpZiBJRSA3XT4NCgkJPGxpbmsgcmVsPSdzdHlsZXNoZWV0JyB0eXBlPSd0ZXh0L2NzcycgaHJlZj0naHR0cDovL3NlbGZzZXJ2aWNlLmFkYS5lZHUuYXovV2ViLy9TaXRlUmVzb3VyY2VzL2Nzcy9maXhfaWU3LmNzcyc+DQoJPCFbZW5kaWZdLS0+ZAIDD2QWBAIDD2QWBgIBD2QWBgIBDxYCHgdWaXNpYmxlaBYCZg8PFgQeB1Rvb2xUaXAFLlNob3BwaW5nIENhcnQgZnVuY3Rpb25hbGl0eSBoYXMgYmVlbiBkaXNhYmxlZC4fAWhkZAIDDxYCHwFoFgJmDw8WBB8CZB8BaGRkAgcPFgIfAWhkAgIPZBYCZg9kFgICAQ8QZA8WAmYCARYCEAUHQ2F0YWxvZwUHQ2F0YWxvZ2cQBQ9Db3Vyc2UgU2VjdGlvbnMFCFNlY3Rpb25zZ2RkAgMPZBYCZg8WAh8BaGQCCQ9kFgICAQ9kFgJmD2QWAgIBD2QWAmYPZBYCZg9kFgJmD2QWAmYPZBYKAgUPDxYCHg9WYWxpZGF0aW9uR3JvdXAFT2N0bDAwX21haW5Db250ZW50X2x2TG9naW5Vc2VyX3VjTG9naW5Vc2VyX2xjTG9naW5Vc2VyX0xvZ2luUGFuZWxMb2dpblZhbGlkYXRpb25kZAILDw8WAh8DBU9jdGwwMF9tYWluQ29udGVudF9sdkxvZ2luVXNlcl91Y0xvZ2luVXNlcl9sY0xvZ2luVXNlcl9Mb2dpblBhbmVsTG9naW5WYWxpZGF0aW9uZGQCDQ8PFgIfAWhkZAIPDw8WAh8DBU9jdGwwMF9tYWluQ29udGVudF9sdkxvZ2luVXNlcl91Y0xvZ2luVXNlcl9sY0xvZ2luVXNlcl9Mb2dpblBhbmVsTG9naW5WYWxpZGF0aW9uZGQCEQ9kFgYCAQ8WAh8BaGQCAw8WAh8BaGQCBQ8WAh8BaGRkBPSjA92gmSk2oiAslp1xJ5IsRSE=")
	                .data("__VIEWSTATEGENERATOR", "D3FEE421")
	                .data("__EVENTVALIDATION",
	                        "/wEdAASnyTluB39m5ovWLMYOyIDU/JPHbbPPXD0WxDgZk+LS48DZH+cho8KQe8zbBR2FC/eRBGfrl8Ay+SgbxgR+Ucly0QtsEYsIVQRn1eMxkHrn4UYhXzg=")
	                .data("ctl00$mainContent$lvLoginUser$ucLoginUser$lcLoginUser$UserName", pc_username)
	                .data("ctl00$mainContent$lvLoginUser$ucLoginUser$lcLoginUser$Password", pc_password)
	                .data("ctl00$mainContent$lvLoginUser$ucLoginUser$lcLoginUser$LoginButton", "Log In")
	                .method(Connection.Method.POST).execute();
	    }

	    public static boolean isLoggedIn(Connection.Response auth)throws IOException {
	        return auth.parse().toString().contains("Welcome");
	    }

	    public static Map<String, String> getCookies(Connection.Response auth){
	        return auth.cookies();
	    }
}
