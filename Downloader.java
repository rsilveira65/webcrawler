/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author gustavowrege
 */

public class Downloader implements Callable<ArrayList<Link>>{
    ArrayList<Link> nextDownloadList;
    String host;
    String get;
    int porta;

    Link linkentrada;
   
    public Downloader(Link url){
        nextDownloadList = new ArrayList<>();
        host = url.getHost();
        get = url.getGet();
        porta = 80;
        linkentrada=url;       
    }

    @Override
    public ArrayList<Link> call() throws Exception {
        return download(); 
    }

    public ArrayList<Link> download() throws IOException{
        ArrayList<Link> downloadList = new ArrayList<>();
        Socket socketCliente = null;
        PrintWriter saidaw = null;
        InputStream entrada = null;
        boolean tagInit = false;
        
       /* Se objeto é marcado como https, ira criar um socket do tipo SSL na porta 443
        caso contrário, ira criar socket normal na porta 80 com timeout*/

        try {
            //Socket HTTPS
            if(linkentrada.isHttps()){
                socketCliente = createSSLSocket(linkentrada);    
            }else{//Socket NORMAL
                socketCliente = new Socket();
                socketCliente.connect(new InetSocketAddress(host, porta), 1000);
            }
            
            saidaw = new PrintWriter(socketCliente.getOutputStream(), true);
            entrada = socketCliente.getInputStream();
            InputStreamReader inr = new InputStreamReader(entrada);
            BufferedReader br = new BufferedReader(inr);
            
            //Envia o GET 
            saidaw.print("GET " + get + " HTTP/1.1\n"); 
           // System.out.println("GET " + get + " HTTP/1.1");
            saidaw.print("Host:"+ host +"\n"); 
            //System.out.println("Host:"+ host +""); 
            saidaw.print("Conncection: close\n"); 
            saidaw.print("\n\n");
            saidaw.flush();
 
            String linha = br.readLine();
            
            if(linha != null && linha.contains("200")){
                
                //Chama método que cria o diretório para salvar paginas
                String parser = saveObj(host, get, false);

                //Cria arquivo
                BufferedWriter fos1 = new BufferedWriter(new FileWriter(parser));
                
                //Comeca leitura do buffer recebido
                while ((linha = br.readLine()) != null){
                    //Define expressoes regulares para filtrar imagens e links
                    Pattern pattern1 = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
                    Pattern pattern2 = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
                    Pattern pattern3 = Pattern.compile("\\s*(?i)href\\s*=\\s*(\\\"([^\"]*\\\")|'[^']*'|([^'\">\\s]+))");
                    Pattern pattern4 = Pattern.compile(".*?(/.*)");
                    
                    //Define as buscas das expressoes regulares
                    Matcher matcherTag = pattern1.matcher(linha);
                    Matcher matcher2 = pattern2.matcher(linha);
                    
                    //Verifica se o HTML começou de fato
                    if(linha.contentEquals(""))
                        tagInit = true;

                    //Filtra as imagens
                    while (matcher2.find()){
                        String img = matcher2.group(1).trim();
                        //System.out.println(matcher2.group(1).trim());
                        String pathImg = "";
                        
                        //Retira p http e https
                        if (img.contains("http://"))
                            img = img.replace("http://", "");

                        if(img.contains("https://"))
                            img = img.replace("https://", "");
                        
                        //Filtra host e get das IMAGENS
                        Matcher matcher3 = pattern4.matcher(img);
                        if(matcher3.find()){
                            String imgGet = matcher3.group(1).trim();
                            String imgHost = matcher3.group(0).trim().replace(imgGet, "");

                            //Quando tem HOST E GET
                            if(!"".equals(imgHost) ){
                              System.out.println("-> Buscando objeto "+WebCrawler.contador+" : "+imgHost+imgGet);
                              WebCrawler.contador++;
                                //pathImg = downloadImg(imgHost,imgGet);
                            }//Sem HOST
                            else{
                                System.out.println("-> Buscando objeto "+WebCrawler.contador+" : "+host+imgGet);
                                WebCrawler.contador++;
                                //pathImg = downloadImg(host,imgGet);
                            }
                        }//Sem GET
                        else if(!(img.contains("javascript") || img.contains("\\"))){
                           System.out.println("-> Buscando objeto "+WebCrawler.contador+" : "+host+"/");    
                           WebCrawler.contador++;
                            //pathImg = downloadImg(host,"/");
                        }
                    }

                    //Filtra o HTML para encontrar <a href="...">   
                    while (matcherTag.find()) {

                        // Pega tudo dentro de <a ...... >
                        String href = matcherTag.group(1);     
                        
                        //Pega a parte do href="...."
                        Matcher matcherLink = pattern3.matcher(href);
                        while(matcherLink.find()){

                            String link = matcherLink.group(1).trim();
                           
                            //Exclui links errados ou desnecessários
                            if (link.contains("\'"))
                                continue;

                            if (link.contains("http://"))
                               link = link.replace("http://", "");

                            if(link.contains(".pdf") || link.contains(".mp3") || link.contains(".avi") || link.contains(".mpg")|| link.contains(".asp"))
                                continue;

                            link = link.replace("\"","");

                            // sem links vazios
                            if (link.length() < 1) {
                                continue;
                            }
                            //sem ancoras
                            if (link.charAt(0) == '#') {
                                continue;
                            }
                            if (link.charAt(0) == '{') {
                                continue;
                            }
                            if (link.charAt(0) == '$') {
                                continue;
                            }
                            if (link.charAt(0) == '/') {
                                //continue;
                            }
                            // sem email links
                            if (link.contains("mailto:")) {
                                continue;
                            }
                           //Separa por HOST e GET
                            Matcher matcher3 = pattern4.matcher(link);
                            if(matcher3.find()){
                                String getLink = matcher3.group(1).trim();
                                String hostLink = matcher3.group(0).trim().replace(getLink, "");

                                //Tem GET E HOST
                                if(!"".equals(hostLink) ){
                                     if(link.contains("https")){
                                        link = link.replace("https://", "");
                                        
                                      
                                      if (link.contains("/")) {
                                            int split = link.indexOf("/");
                                                hostLink = link.substring(0, split);
                                                getLink = link.substring(split);

                                             if(!getLink.endsWith("/"))
                                                    getLink = getLink+"/";
                
                                               }else {
                                                    hostLink = link;
                                                     getLink = "/";
                                                 }
                                     
                                     
                                        Link q = new Link(hostLink, getLink);
                                        q.setaHttps();
               
                                        downloadList.add(q);
                                         socketCliente = createSSLSocket(q);
                                       
                                        //pathLink = saveObj(hostLink, getLink, false);
                                    }else{ //caso nao seja Https consinua o boolean "false" no objeto
                                        
                                        
                                         Link q = new Link(hostLink, getLink);
                                        downloadList.add(q);
                                    
                                    }
                                  System.out.println("-> Buscando objeto "+WebCrawler.contador+" : "+hostLink+getLink);
                                  WebCrawler.contador++;
                                }//NAO TEM HOST
                                else{       
                                           if(link.contains("https")){
                                                 Link q= new Link(host,getLink);
                                                 q.setaHttps(); //seta boolean Https "true" no objeto
                                                 downloadList.add(q);
                                                 socketCliente = createSSLSocket(q);
                                                 //pathLink = saveObj(host, getLink,false);
                                            }
                                            else{
                                                      Link q = new Link(host, getLink);
                                                      downloadList.add(q);
                                                      
                                            }
                                
                                     System.out.println("-> Buscando objeto "+WebCrawler.contador+" : "+host+getLink);
                                     WebCrawler.contador++;       
                                    }
                            }//NAO TEM GET
                            else if(!(link.contains("javascript") || link.contains("\\"))){
                                if(link.contains("https")){
                                    link = link.replace("https://", "");
                                    Link q= new Link(link,"/");
                                    q.setaHttps();
                                    downloadList.add(q);
                                    socketCliente = createSSLSocket(q);
                                    //pathLink = saveObj(link, "/",false);
                                }
                                else{
                                    Link q= new Link(link,"/");
                                    downloadList.add(q);

                                }
                            }System.out.println("-> Buscando objeto "+WebCrawler.contador+" : "+link+"/");
                             WebCrawler.contador++;
 
                        }

                    }
                    //Escreve html no arquivo criado
                    if(tagInit){
                        fos1.write(linha+"\n");
                    }

                }
   
                
            }

        } catch (UnknownHostException e) {
            //System.err.println("Host nao encontrado:" + host + get);
            return downloadList;
        }catch (IOException e) {
            //System.err.println("Nao foi possivel encontrar io para o host:" + host + get);
            return downloadList;
        }

        return downloadList;
    }
    
    //Metodo que salva as imagens
    public String downloadImg(String host, String get){
        Socket socketImg = null;
        PrintWriter saidaw = null;
        InputStream entrada = null;
        
        String pathImg = saveObj(host, get, true);
        //System.out.println(pathImg);
        try {
            socketImg = new Socket();
            socketImg.connect(new InetSocketAddress(host, porta), 1000);
            
            //Aloca buffer de Resposta
            saidaw = new PrintWriter(socketImg.getOutputStream(), true);
            entrada = socketImg.getInputStream();
            //Envia o GET 
            saidaw.print("GET " + get + " HTTP/1.1\n"); 
            saidaw.print("Host:"+ host +"\n"); 
            saidaw.print("\n\n");
            saidaw.flush();
            
            ByteArrayOutputStream oim = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            try {
                while (-1 != (n = entrada.read(buf))) {
                    oim.write(buf, 0, n);
                }
                saidaw.close();
                entrada.close();
                byte[] response = oim.toByteArray();
                FileOutputStream fos = new FileOutputStream(pathImg);
                fos.write(response);
                fos.close();

                BufferedReader br = null;
                String cl = null;
                String[] slited = null;
                br = new BufferedReader(new FileReader(pathImg));
                while ((cl = br.readLine()) != null) {
                    if (cl.contains("Length")) {
                        slited = cl.split(": ");
                        break;
                    }
                }


                boolean flag = false;
                int dif = response.length - Integer.parseInt(slited[1]);
                byte[] newb = new byte[dif];
                FileInputStream isf = new FileInputStream(pathImg);
                ByteArrayOutputStream oim1 = new ByteArrayOutputStream();
                while ((n = isf.read(newb)) != -1) {
                    if (flag) {
                        oim1.write(newb, 0, n);
                    }
                    flag = true;
                }

                byte[] response1 = oim1.toByteArray();
                FileOutputStream fos1 = new FileOutputStream(pathImg);
                fos1.write(response1);
                fos1.close();
            }catch (IOException e) {    }
            
            
            
            
        } catch (UnknownHostException e) {
            //System.err.println("Host nao encontrado:" + host + get);
            return (host+get);
        } catch (IOException e) {
            //System.err.println("Nao foi possivel encontrar io para o host:" + host + get);
            return (host+get);
        }

        return pathImg;
        
    }
    
    //Metodo que cria os diretorios e retorna o caminho para criar arquivos
    public String saveObj(String host, String get, boolean isImg) { 
        Calendar c = Calendar.getInstance();
        
        Pattern path = Pattern.compile("(.*/)*+(.*)");
        Matcher matchPath = path.matcher(get);
        if (matchPath.find()) {
            File f = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Crawled: "+c.get(Calendar.DAY_OF_MONTH)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR) + System.getProperty("file.separator") + host + matchPath.group(1));
            //System.out.println(System.getProperty("user.dir") + System.getProperty("file.separator") + "Crowled: "+c.get(Calendar.DAY_OF_MONTH)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR) + System.getProperty("file.separator") + host + matcha.group(1));
            f.mkdirs();
        }
        String Parse = System.getProperty("user.dir") + System.getProperty("file.separator") + "Crawled: "+c.get(Calendar.DAY_OF_MONTH)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR) + System.getProperty("file.separator") + host + get;
        
        if(!isImg){
            Parse = Parse.replace("?", "~");
            Parse = Parse.replace("\\", "/");
            if (get.endsWith("/"))
                Parse = Parse + "index.html";
            else
                Parse = Parse + ".html";             
        }
        
        return Parse;
    }
     
    /* Método que printa os certificados dos links. O parametro booleando autoassinado, é usado para
            identificar os sites auto assinados*/
    
    public void printaCertificados(Link a, SSLSocket socket,boolean autoassinado){
        try {        
            String certificado, assinatura;
            Pattern pat = Pattern.compile("O=\"+(.*)+\"");
            Matcher mat = pat.matcher(socket.getSession().getPeerCertificateChain()[0].getIssuerDN().getName());
            if(mat.find()){
                assinatura = mat.group();
                assinatura = assinatura.replace("\"", "");
                assinatura = assinatura.replace("O=", "");
            }else{
                assinatura = "Organização não encontrada!";
            }
            
            certificado = "\t" +socket.getSession().getPeerCertificateChain()[0].getSubjectDN().getName()+ "\n";
            certificado = certificado.replace("CN=", " Nome: ");
            certificado = certificado.replace("O=", "Organizacao: ");
            certificado = certificado.replace("C=", "Pais: ");
            certificado = certificado.replace("OU=", "Unidade: ");
            certificado = certificado.replace(",", "\n\t");
            certificado = certificado.replace("ST=", "Estado: ");
            certificado = certificado.replace("L=", "Local: ");
            certificado = certificado.replace("=", ": ");
            System.err.println("\n==================================| CERTIFICADO |==================================");
            
            if(autoassinado){
                System.err.println("\t" +" Site: " +a.retornaLink()+"\n"+certificado+"\t -> Certificado  Auto assinado por: "+assinatura);
            }else{
                System.err.println("\t" +" Site: " +a.retornaLink()+"\n"+certificado+"\t -> Certificado  assinado "+assinatura);

            }
            System.err.println("==================================================================================\n");
        } catch (SSLPeerUnverifiedException ex) {
            
        }
        
    }
    
      /* Método que cria um SSL socket utilizando as classes SSLSocketFactory e SSLSocket */
   
    public Socket createSSLSocket(Link c){
        String entradalink = c.getHost();                       
        SSLSocket socket = null;
        SSLSocketFactory socketFact;
        socketFact = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {socket = (SSLSocket) socketFact.createSocket(entradalink, 443);} catch (IOException ex) { return null;}
        
        /* Tenta conexão com https,caso rejeitar a conexão (exceção), o servidor HTTPS alvo tenta autenticar-se com um certificado não confiável.  */
        try {
            socket.startHandshake();    
            printaCertificados(c, socket,false); //printa certificado 
        /* Caso seja não confiável (auto-assinado), cria-se um socket usando tecnica trustManager(promiscuous) que aceita todo tipo de conexão. referencia http://www.pwntester.com/2012/11/05/fourgoats-vulnerabilities-promiscuous-ssl-trustmanager/*/
        } catch (IOException ex) {    
            try {                        
                    SSLSocketFactoryProm sFP ;
                    sFP = new SSLSocketFactoryProm();
                    socket = (SSLSocket) sFP.createSocket(c.getHost(), 443);
                    printaCertificados(c, socket,true); //printa certificado auto assinado
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }  }
        
        return (Socket) socket;
    }
}