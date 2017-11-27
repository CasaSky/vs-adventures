package de.haw.vsadventures;

import com.google.gson.Gson;

import de.haw.vsadventures.entities.*;
import de.haw.vsadventures.utils.ApacheClient;

import de.haw.vsadventures.utils.UDPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Scanner;

public class Client {

    @Autowired
    private RestTemplate restTemplate;

    private String url = "http://172.19.0.7:5000"; //blackboard
    private String quest1 = url+"/blackboard/quests/1";
    private String deliveries = quest1+"/deliveries";
    private String login = url+"/login";
    private String users = url+"/users";

    private Scanner scanner = new Scanner(System.in);

    private User user = new User();

    private String loginToken;

    private Gson g = new Gson();

    public Client() {
        this.restTemplate = new RestTemplate();
    }

    public Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void register() {

        System.out.println("Choose a username");
        user.setName(scanner.next());
        System.out.println("Choose a password");
        user.setPassword(scanner.next());


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String userJson = g.toJson(user);
            HttpEntity<String> entity = new HttpEntity<>(userJson, headers);
        try {

            ResponseEntity<String> loginResponse = restTemplate
                    .exchange(users, HttpMethod.POST, entity, String.class);
            System.out.println("Registration was succesfull!");
        } catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {

                System.out.println("Register failed!");
                throw new RuntimeException(httpClientOrServerExc);
        }
    }

    public void login() {

        System.out.println("Enter your Username");
        user.setName(scanner.next());
        System.out.println("Enter your password");
        user.setPassword(scanner.next());

        restTemplate.getInterceptors().add(
                new BasicAuthorizationInterceptor(user.getName(), user.getPassword()));
        try {

            ResponseEntity<LoginResponse> response = restTemplate.exchange(login, HttpMethod.GET, null, LoginResponse.class);
            LoginResponse restCall = response.getBody();
            loginToken = restCall.getToken();
            System.out.println("This is your login token: "+ loginToken);
        } catch (HttpStatusCodeException exception) {

            System.out.println("Youre cridentials are wrong, please verify.");
        }
    }


    public void playFirstQuest() throws Exception {

        // looks for the first task.
        String firstTask = lookForFirstTask();

        // looks for resource and location
        ResponseEntity<Task> responseTask = restTemplate.getForEntity(url+firstTask,Task.class, Task.class);
        TaskPosition taskPosition = responseTask.getBody().getObject();
        System.out.println("Boss said: "+taskPosition.getDescription()); // Anzeige von task Description
        String resource = taskPosition.getResource(); // resource
        String location = taskPosition.getLocation(); // location

        // looks for quest host
        ResponseEntity<Location> responseMap = restTemplate.getForEntity(url+location,Location.class, Location.class);
        LocationPosition locationPosition = responseMap.getBody().getObject();
        System.out.println("The game is taking you to the "+locationPosition.getName()+" ..."); // Anzeige von Location name
        String hostQuest = "http://"+locationPosition.getHost(); // host of quest


        // Alternative zum Resttemplate, wegen Kopfschmerzen bei Eingabe von Token Auth Header --> Implementierung unsauber, Doku schlecht.
        ApacheClient ac = new ApacheClient();

        // looks for quest message
        String questMessage = ac.get(hostQuest+resource, loginToken, "message");
        System.out.println("Boss question: "+questMessage);
        System.out.print("Answer here: ");
        String answer = scanner.next();

        // looks for quest token
        String questToken = ac.post(hostQuest+resource,loginToken,answer,"token");

        // finish!
        firstTask = firstTask.substring(1); // remove slash
        String request = "{\"tokens\":{\""+firstTask+"\":\""+questToken+"\"}}";
        String finalMessage = ac.post(deliveries, loginToken, request, "message");
        System.out.println("Boss said: "+finalMessage);
        System.out.println("Quest finished!");
        System.out.println("__________________");
    }

    private String lookForFirstTask() {

        ResponseEntity<Quest> responseQuest = restTemplate.exchange(quest1, HttpMethod.GET,null, Quest.class);
        QuestPosition questPosition = responseQuest.getBody().getObject();
        System.out.println("Boss said: "+questPosition.getDescription()); // Anzeige von firstTask Description
        List<String> tasksUrls = questPosition.getTasks();
        return tasksUrls.get(0);
    }

    public void menu() throws Exception {

        String input="";

        while (!input.equals("q")) {

            System.out.println("\n___________________");
            System.out.println("Adventurer menu:");
            System.out.println("1: Play first quest");
            System.out.println("2: Login");
            System.out.println("3: Register");
            System.out.println("q: Quit");
            input = scanner.next();


            switch (input) {
                case "1":
                    if (loginToken==null) {System.out.println("Please login first.");}
                    else {playFirstQuest();} break;
                case "2": login(); break;
                case "3": register(); break;
                case "q": break;
            }
        }
        System.out.println("Goodbye!");
    }
}
