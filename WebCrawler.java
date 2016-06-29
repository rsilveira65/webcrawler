/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.GregorianCalendar;

/**
 *
 * @author gustavowrege
 */
public class WebCrawler {
    ArrayList<Link> downloadList;
    ArrayList<Link> nextDownloadList;
    ArrayList<Link> downloadedList;
    int profundidade;
    int porta;
    boolean validade=false;
    String url;
    static int contador;

    public WebCrawler(int profundidade, String url){
        this.profundidade = profundidade;
        this.url = url;
        porta = 80;
    }
    
    public void runWebCrawler(){
        //Listas de Downloads, próximos Downloads e Links Baixados
        downloadList = new ArrayList<>();
        nextDownloadList = new ArrayList<>();
        downloadedList = new ArrayList<>();
        
       //aceitando primeiro link do tipo "http:// ..." e "https://..."
        String hostURL;
        String getURL;
        if (url.contains("http://")){
              url = url.replace("http://", "");
              this.validade=false;
        }
        if (url.contains("https://")){
              url = url.replace("https://", "");
              this.validade=true;
        }
              
        if (url.contains("/")) {
            int split = url.indexOf("/");
            hostURL = url.substring(0, split);
            getURL = url.substring(split);

            if(!getURL.endsWith("/"))
                getURL = getURL+"/";
                
        } else {
            hostURL = url;
            getURL = "/";
        }
        
        //Pool de 8 threads
        ExecutorService poolThreads = Executors.newFixedThreadPool(8);
        
        //Lista de Retornos das threads
        ArrayList<Future<ArrayList<Link>>> returnList = new ArrayList<>();
        //Future<ArrayList<Link>> retorno;

        try {
            Date d = GregorianCalendar.getInstance().getTime();  
            SimpleDateFormat format = new SimpleDateFormat();  
            
            //Adiciona o primeiro URL a Lista de Download
            Link p = new Link(hostURL,getURL);
            if (validade)p.setaHttps();
            downloadList.add(p); //adiciona primeiro site
            System.out.println("\t\t| WEBCRAWLER |\n");
            System.out.println("\t Iniciado em: "+ format.format(d));
            System.out.println("\t Profundidade definida: "+profundidade+"\n"+".\t Aguarde os objetos serem baixados...\n"); 
           
            int j=0;
            for(int i=0; i<=profundidade; i++){
                while(!downloadList.isEmpty()){
                    Link link = downloadList.remove(0);
          
                   
                    if(!downloadedLink(link)){
                        downloadedList.add(link);
                  
                        
                        Future<ArrayList<Link>> retorno;
                        
                        retorno = poolThreads.submit(new Downloader(link));
                        
                        returnList.add(retorno);                        
                    }
                       
                }
                while(!returnList.isEmpty()){
                    nextDownloadList.addAll(returnList.remove(0).get());  
                }
                System.out.println("\n\n\n\t# Profundidade: "+i+" concluida!");
                System.out.println("\tForam tentados baixar: "+downloadedList.size()+" links!");
                System.out.println("\tA próxima profundidade tem: "+nextDownloadList.size()+" links!\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
                downloadList = nextDownloadList;
                nextDownloadList = new ArrayList<>();
            }
            
        } catch (InterruptedException | ExecutionException ex ) {
            //System.err.println("Terminated!");
        }
        poolThreads.shutdown();
    }
    
    public boolean downloadedLink(Link link){
        String sLink = link.getHost()+link.getGet();
        for(Link l:downloadedList ){
            String sdowLink = l.getHost()+l.getGet();
            if(sLink.equals(sdowLink))
                return true;
        }
        return false;
    }
}