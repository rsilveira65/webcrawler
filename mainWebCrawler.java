
import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 *
 * @author gustavowrege
 */
public class mainWebCrawler {
    
 
    public static void main(String[] args) throws IOException, TimeoutException {
    
        //int profundidade = 2; //Profundidade
        int profundidade = Integer.parseInt(args[0]);
        //String url = "www.clicrbs.com.br/rs/";
        //String url ="https://ccl.northwestern.edu/netlogo/"; //HTTPS
        //String url = "https://www.pcwebshop.co.uk/"; //auto assinado
        String url = args[1];
        WebCrawler crawler = new WebCrawler(profundidade, url);
        crawler.runWebCrawler();
        
    }
   
}