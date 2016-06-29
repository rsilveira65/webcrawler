/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gustavowrege
 */
public class Link {
    String Host;
    String Get;
    boolean Https;
    
   
    public Link(String Host, String Get)    {
        this.Host = Host;
        this.Get = Get;
        this.Https = false;  
    }

    public String getHost() {
        return Host;
    }

    public String getGet() {
        return Get;
    }
    
     public boolean isHttps() {
        return Https;
    }
    
    public void setaHttps(){
        this.Https = true;
    }
    
     public String retornaLink() {
        return (this.Https?"https://":"http://") +Host+Get;
    }
       
}
