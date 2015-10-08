import java.awt.image.ConvolveOp;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

import org.xml.sax.helpers.ParserFactory;

public class Test_SGBD {

	public static void main(String[] args) throws IOException {
		CriarArquivo();
		GravarIndice("1,2,3,4,5","3","1");
		//Teste de Insert
		String rowID = "200";
		String texto = "Bla Bla bla \n";
		//
		GravaDataBlock( rowID, texto);
		
		byte[] dados = new byte[rowID.getBytes().length + texto.getBytes().length];
				
		
		
		
				
		
		RecuperaIndice();
		//LeituraArquivo();        	
	}

	public static void GravaDataBlock(String rowID, String texto) throws IOException{
		RandomAccessFile leitura = new RandomAccessFile("arquivo.bin", "rw");
		//Hash Map
		HashMap <Integer, String>  mapDataBlock = new HashMap<Integer, String>();
		//Posi��o de refer�ncia do valor dentro do rowid
		int posrowid=0;
		//Auxiliar para ler os dados do byte	
		StringBuilder sbDados = new StringBuilder();
		//Criar um bytes auxiliares
		byte[] teste = new byte[4096];
		//Pega a posi��o de mem�ria do dado dentro do datablock 1;5 ficar� so o 5
		int refPos =Integer.parseInt(rowID.split(";")[1]);		
		//Referencia para ler a posi��o inicial do 4kb
		int posIni = 4096 * Integer.parseInt(rowID.split(";")[0]); // Restar� so 1 do 1;5
		posIni =4096 - posIni;
		//passagem do bytes vazios e retorno dele preenchido com os dados daquela posi��o espec�fica
		leitura.read(teste,posIni,teste.length);
		//Percorrer e converter o bytes
		for (int i = 0; i < teste.length ; i++){
			//Diferente de dados em branco 0 bytes
			 if(teste[i] != 0){
				 //Se n�o for \n como refer�ncia de parada 
				 if(teste[i] != 10){
					 char cLetra = (char)teste[i];
					 sbDados.append(cLetra);
				 }else{
					 //Adiciona os dados no map 
					 mapDataBlock.put(posrowid,sbDados.toString());
					 posrowid ++;
				 }
			 }
		}
		//Grava na primeira posi��o os dados
		if(sbDados.toString().length() == 0){
			leitura.write(sbDados.toString().getBytes(),posIni,sbDados.toString().getBytes().length);
		}else{
			//ir� dar um update no arquivo naquela posi��o 
			if(mapDataBlock.containsKey(refPos)){
				mapDataBlock.put(refPos,sbDados.toString());			
			}else{
				//TODO Gravara na ultima posi��o				
			}
		}
		
		//TODO ver apartir dos dados como fazer a substitui��o
		
		
	}
	
	
	public static void LeituraArquivo(int iOpcao ,HashMap mapa, int rowID) throws IOException{
	RandomAccessFile leitura = new RandomAccessFile("arquivo.bin", "rw");
	byte[] teste = new byte[4096];
	StringBuilder sbDados = new StringBuilder();
	int posIni = 0;
	int posFim = 4096; // 4k
	int size = (int) leitura.length();
	
	//Gambiarra para ler a posi��o do datablock
	posIni = 4096 - posFim;
	posFim = posFim * rowID;
	
	//1 - Indice, 2 - DataBlock espec�fico
	switch (iOpcao) {
	//Retorna o Indice em um hashMap
	case 1:
		int icountZero= 0;
		leitura.read(teste, posIni, posFim);
		for (int i = 0; i < teste.length ; i++){
			 if(teste[i] != 0){
				char cLetra = (char)teste[i];
		   		sbDados.append(cLetra); 
			 }else {
				 
				 if (icountZero == 0 && sbDados.toString().length() > 0){
					 //Armazena a lista FREE
					  mapa.put("free", sbDados.toString());
					  //Limpa a vari�vel
					  sbDados = new StringBuilder();
						 icountZero ++; 
				 }else if (sbDados.toString().length() > 0 && icountZero == 1){
					  mapa.put("root", sbDados.toString());	
					  //Limpa a vari�vel
					  sbDados = new StringBuilder();
					   icountZero ++; 
				 }
				 else if(sbDados.toString().length() > 0 && icountZero > 1){
					 mapa.put("table", sbDados.toString());	
					  //Limpa a vari�vel
					  sbDados = new StringBuilder();
					   break;  
					
				 }
			}
		 }	
		break;
	//CASO QUE RETORNA O DATABLOCK ESPEC�FICO
	case 2:
		while(size > 0 && posIni < size){
			leitura.read(teste, posIni, posFim);
			for (int i = 0; i < teste.length ; i++){
				 if(teste[i] != 0){
					char cLetra = (char)teste[i];
			   		sbDados.append(cLetra); 
				 }              		    
			 }
				System.out.print(sbDados.toString());
				size -= posFim;
				posIni += posFim + 1;
				posFim = posFim + posFim;	
			}
		
		   leitura.close();	   
	}


	}

	public static void GravarIndice(String listFree, String root, String table) {
	/*###################### GRAVACAO DE DADOS ######################*/
	try{
		//Acesso randomico 
		RandomAccessFile escrita = new RandomAccessFile("arquivo.bin", "rw");
		//Referencias
		String sfree = listFree;
		String sroot = root;
		String stable = table;
		//Colocando valores nas posi��es
		escrita.seek(0);
		escrita.write(sfree.getBytes("utf-8"));
		
		escrita.seek(1001);
		escrita.write(sroot.getBytes("utf-8"));
		
		escrita.seek(1021);
		escrita.write(stable.getBytes("utf-8"));
		
		//Fecha o arquivo
		escrita.close();	
		
	}catch(FileNotFoundException e){
		e.printStackTrace();
		System.out.println("Erro = "+e);  
	}catch(IOException e){
		e.printStackTrace();
		System.out.println("Erro = "+e);  
	}	
	}
	

	public static HashMap RecuperaIndice() throws IOException{
		DataBlock dtBlock =  new DataBlock();
		HashMap <String, String>  mapa = new HashMap<String, String>();
		LeituraArquivo(1,mapa,1);
		return mapa;	
	}

	public static void CriarArquivo() {
	try{
		FileOutputStream out = new FileOutputStream("arquivo.bin");  
		ObjectOutputStream os = new ObjectOutputStream(out);
		//Especifica o tamanho do arquivo 256MB
		byte[] buf = new byte[260884000];
		os.write(buf);
		os.flush();
		os.close();	 		
	
	}catch(FileNotFoundException e){
		e.printStackTrace();
		System.out.println("Erro = "+e);  
	}catch(IOException e){
		e.printStackTrace();
		System.out.println("Erro = "+e);  
	}catch(IndexOutOfBoundsException e){
		e.printStackTrace();
		System.out.println("Erro = "+e);  
	}		
	}	
	
	
}

