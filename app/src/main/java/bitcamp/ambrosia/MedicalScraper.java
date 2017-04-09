//package "C:\\Users\\User\\Documents\\GitHub\\Ambrosia\\app\\libs\\jsoup-1.10.2.jar";
import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;


public class MedicalScraper
{
	
	public String getLoaded(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF); /* comment out to turn off annoying htmlunit warnings */
        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage(url);
        webClient.waitForBackgroundJavaScript(30 * 1000); /* will wait JavaScript to execute up to 30s */

        String pageAsXml = page.asXml();
		return pageAsXml;
	}

	
	protected String[][] getProvider(String type, String lat, String lon)
	{
		Document doc;
		String url = "http://doctor.webmd.com/results?lat=" + lat + "&lon=" + lon + "&ln=" + type;
		String htmlFile;
		try
		{
			htmlFile = getLoaded(url);
		}
		catch(MalformedURLException e)
		{
			return null;
		}
		catch(IOException e)
		{
			return null;
		}

		doc =  Jsoup.parse(htmlFile);
		String link1, link2, link3;
		
		Elements list = doc.getElementsByClass("result");
		
		Document[] doctorPages = new Document[3];
		
		String[][] result = new String[3][3];
		
		System.out.println(doc);
		System.out.println("/n");
		
		for(int i = 0; i < 3; i++)
		{
			Elements links = list.get(i).getElementsByTag("a");
			String link = links.first().attr("href");
			url = "http://doctor.webmd.com" + link;
			String htmlFile2;
			try
			{
				htmlFile2 = getLoaded(url);
			}
			catch(MalformedURLException e)
			{
				return null;
			}
			catch(IOException e)
			{
				return null;
			}
			doctorPages[i] = Jsoup.parse(htmlFile2);
		}
		for(int i = 0; i < 3; i++)
		{
			Element block = doctorPages[i].getElementsByClass("content photo").first();
			Element name = block.select("span.provider").first();
			result[i][0] = name.text();
			Element addr = block.getElementsByClass("address-block").first();
			Elements fields = addr.getElementsByTag("span");
			result[i][1] = fields.first().text() + "\n" + fields.get(1).text() + " " + fields.get(2) + " " + fields.get(3);
			Element phone = block.select("a.phone").first();
			result[i][2] = phone.text();
		}
		
		return result;
	}
	
}
