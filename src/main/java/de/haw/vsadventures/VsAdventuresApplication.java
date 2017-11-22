package de.haw.vsadventures;

import de.haw.vsadventures.utils.UDPClient;
import javafx.beans.binding.When;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@SpringBootApplication
public class VsAdventuresApplication {

	public static RestTemplate restTemplate;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		RestTemplate template = builder.build();
		List<HttpMessageConverter<?>> converters = template.getMessageConverters();
		for(HttpMessageConverter<?> converter : converters){
			if(converter instanceof MappingJackson2HttpMessageConverter){
				try{
					((MappingJackson2HttpMessageConverter) converter).setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return template;
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) {
		return args -> {
			this.restTemplate = restTemplate;
			/*UDPClient udpClient = new UDPClient();
			udpClient.start();
			udpClient.join();
			String msg = udpClient.getMsg();
			Client client = new Client(restTemplate);
			client.menu();*/
		};
	}

	public static void main(String[] args) {

		MulticastSocket socket = null;
		try {
			//set Network Interface
			NetworkInterface nif = NetworkInterface.getByName("tun0");
			if(nif==null){
				System.err.println("Error getting the Network Interface");
				return;
			}
			System.out.println("Preparing to using the interface: "+nif.getName());
			//Enumeration<InetAddress> nifAddresses = nif.getInetAddresses();


			InetAddress inetAddress = null;
			for (Enumeration<InetAddress> enumIpAddr =
				 nif.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {

				inetAddress = enumIpAddr.nextElement();
				String ipAddr = inetAddress.toString();


				if(ipAddr.startsWith("/192.168")){
					//inetAddress = enumIpAddr.nextElement();
				}
			}


			InetSocketAddress inetAddr= new InetSocketAddress(inetAddress,24000);

			socket = new MulticastSocket(inetAddr);
			socket.setNetworkInterface(nif);
			String msg;

			System.out.println("Listen on " + socket.getLocalAddress() + " from " + socket.getLocalSocketAddress() + " port " + socket.getBroadcast());
			byte[] buf = new byte[512];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			while (true) {
				System.out.println("Waiting for data");
				socket.receive(packet);
				System.out.println("Data received");
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}





		Client client = new Client(restTemplate);
		try {
			client.menu();
		} catch (Exception e) {
			e.printStackTrace();
		}
		SpringApplication.run(VsAdventuresApplication.class, args);
	}


}
