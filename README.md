# WEBCRAWLER
## Implementação:
	O WebCrawler desenvolvido recebe uma url e a profundidade a ser acessada.
	Com isso, no primeiro momento ele entra na url passada e encontra todos os links presentes no HTML, cria o diretório na máquina do cliente e salva a página html principal e suas imagens.
	Após, distribui entre as threads os links encontrados através de um Pool de threads. Uma verificação se o link já foi baixada é feita, a partir de então, cada thread baixa recursivamente a url passada assim como na primeira etapa. Isto é feita até a profundidade definida.
	Foi utilizada um timeout no socket para não travar demais o programa.
	

	*Obs: as imagens não estão sendo baixadas de fato para não demorar muito a execução, entretanto é possível realizar esta função apenas descomentado-a no código fonte.

	Na segunda etapa do trabalho, foi implementada a possibilidade de fazer conexão com sites HTTPS, usando SSL. Para isso, utilizamos as classes SSLsocket e SSLSocketFactory.
	Utilizando essas classes, o webcrawler realiza em tempo de execução qual das conexões usar com base no url.

## Execução:
	Para executar:
	```bash
		-make
		-./executeme profundidade url
	```
	
	
## Testes Realizados:
	Foram realizados testes com  os seguintes links:
	
		- https://ccl.northwestern.edu/netlogo/
		- https://www.pcwebshop.co.uk/   PS: Auto Assinado
		- http://portal.ufpel.edu.br/
		- http://avainstitucional.ufpel.edu.br/
		- http://www.globo.com
		- http://www.clicrbs.com.br/rs/