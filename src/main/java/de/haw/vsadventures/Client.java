package de.haw.vsadventures;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import de.haw.vsadventures.entities.*;
import de.haw.vsadventures.utils.ApacheClient;

import de.haw.vsadventures.utils.UDPClient;
import org.apache.log4j.Logger;
import org.json.JSONObject;
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

    private ApacheClient ac = new ApacheClient();

    private Logger logger = Logger.getLogger(Client.class);

    private String heroUrl = "172.19.0.35:8080/hero";

    String taskText = null;
    String resourceText = null;
    String methodText = null;
    String dataText = null;
    String callbackText = null;
    String messageText = null;
    ObjectMapper mapper = new ObjectMapper();


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

            restTemplate
                    .exchange(users, HttpMethod.POST, entity, String.class);
            logger.info("Registration was succesfull!");
        } catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {

                logger.error("Register failed!");
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
            logger.info("This is your login token: "+ loginToken);
        } catch (HttpStatusCodeException exception) {

            logger.error("Youre cridentials are wrong, please verify.");
        }
    }


    public void playFirstQuest() throws Exception {

        // looks for the first task.
        String firstTask = lookForFirstTask();

        // looks for resource and location
        ResponseEntity<Task> responseTask = restTemplate.getForEntity(url+firstTask,Task.class, Task.class);
        TaskPosition taskPosition = responseTask.getBody().getObject();
        logger.info("Boss said: "+taskPosition.getDescription()); // Anzeige von task Description
        String resource = taskPosition.getResource(); // resource
        String location = taskPosition.getLocation(); // location

        // looks for quest host
        ResponseEntity<Location> responseMap = restTemplate.getForEntity(url+location,Location.class, Location.class);
        LocationPosition locationPosition = responseMap.getBody().getObject();
        logger.info("The game is taking you to the "+locationPosition.getName()+" ..."); // Anzeige von Location name
        String hostQuest = "http://"+locationPosition.getHost(); // host of quest


        // looks for quest message
        String questMessage = ac.get(hostQuest+resource, loginToken, "message");
        System.out.println("Boss question: "+questMessage);
        System.out.print("Answer here: ");
        String answer = scanner.next();

        // looks for quest token
        String questToken = ac.post(hostQuest+resource,loginToken,answer,"token");

        // finish!
        String finalMessage = deliverQuestToken(firstTask, questToken);
        logger.info("Boss said: "+finalMessage);
        logger.info("Quest finished!");
        logger.info("__________________");
    }

    private String deliverQuestToken(String firstTask, String questToken) {

        firstTask = firstTask.substring(1); // remove slash

        JSONObject tokenValue = new JSONObject();
        tokenValue.put(firstTask, questToken);

        JSONObject tokensObject = new JSONObject();
        tokensObject.put("tokens", tokenValue);
        String request = tokensObject.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.getInterceptors().add(
                new BasicAuthorizationInterceptor(user.getName(), user.getPassword()));

        HttpEntity<String> entity = new HttpEntity<>(request, headers);


        ResponseEntity<ObjectNode> response;
        String message = null;
        try {
            response = restTemplate.exchange(deliveries, HttpMethod.POST, entity, ObjectNode.class);
            message = response.getBody().get("message").asText();
        } catch (Exception e) {
            message = e.getMessage();
            logger.error(message);
        }
        return message;
    }

    private String lookForFirstTask() {

        ResponseEntity<Quest> responseQuest = restTemplate.exchange(quest1, HttpMethod.GET,null, Quest.class);
        QuestPosition questPosition = responseQuest.getBody().getObject();
        System.out.println("Boss said: "+questPosition.getDescription()); // Anzeige von firstTask Description
        List<String> tasksUrls = questPosition.getTasks();
        return tasksUrls.get(0);
    }

    public void hiring() {

        String hiringUrl = heroUrl + "/hiring";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String groupText = null;
        String questText = null;
        String messageText = null;
        try {
            groupText = mapper.writeValueAsString("group");
            questText = mapper.writeValueAsString("quest");
            messageText = mapper.writeValueAsString("message");
        } catch (JsonProcessingException e) {
        }

        System.out.println("Enter your group number:");
        String groupUrl = url + "/taverna/groups/" + scanner.next();
        System.out.println("Enter the quest number:");
        String questUri = "/quest/" + scanner.next();
        System.out.println("Enter your message:");
        String message = scanner.next();

        String post = "{" + groupText + ":" + groupUrl + ","+
                questText + ":" + questUri + ","+
                messageText + ":" + message + "}";

        HttpEntity<String> entity = new HttpEntity<>(post, headers);


        try {

            ResponseEntity<Object> response = restTemplate.exchange(hiringUrl, HttpMethod.POST, entity, Object.class);

        } catch (HttpStatusCodeException exception) {

        }
    }

    private void initAssignment() {

        try {
            taskText = mapper.writeValueAsString("task");
            resourceText = mapper.writeValueAsString("resource");
            methodText = mapper.writeValueAsString("method");
            dataText = mapper.writeValueAsString("data");
            callbackText = mapper.writeValueAsString("callback");
            messageText = mapper.writeValueAsString("message");
        } catch (JsonProcessingException e) {
        }
    }

    public void assignment() {

        String assignmentUrl = heroUrl + "/assignment";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

       initAssignment();

        System.out.println("Enter your task number:");
        String taskUrl = "/blackboard/tasks/" + scanner.next();
        System.out.println("Enter the resource name:");
        String resource = "/" + scanner.next();
        System.out.println("Enter the method type:");
        String method = "/" + scanner.next();
        System.out.println("Enter your callback address url:");
        String callbackUrl = scanner.next();
        System.out.println("Enter your message:");
        String message = scanner.next();

        String post = "{" + taskText + ":" + taskUrl + ","+
                resourceText + ":" + resource + "," +
                methodText + ":" + method + "," +
                dataText + ":" +""+ "," +
                callbackText + ":"+ callbackUrl+ ","+
                messageText + ":" + message +"}";

        HttpEntity<String> entity = new HttpEntity<>(post, headers);


        try {

            ResponseEntity<Object> response = restTemplate.exchange(assignmentUrl, HttpMethod.POST, entity, Object.class);

        } catch (HttpStatusCodeException exception) {

        }
    }

    public void election() {

        String electionUrl = heroUrl + "/election";
        ObjectMapper mapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        String algorithmText = null;
        String payloadText = null;
        String userText = null;
        String jobText = null;

        try {
            algorithmText = mapper.writeValueAsString("task");
            payloadText = mapper.writeValueAsString("resource");
            userText = mapper.writeValueAsString("method");
            jobText = mapper.writeValueAsString("");
        } catch (JsonProcessingException e) {
        }

        initAssignment();

        System.out.println("Enter your task number:");
        String taskUrl = "/blackboard/tasks/" + scanner.next();
        System.out.println("Enter the resource name:");
        String resource = "/" + scanner.next();
        System.out.println("Enter the method type:");
        String method = "/" + scanner.next();
        System.out.println("Enter your callback address url:");
        String callbackUrl = scanner.next();
        System.out.println("Enter your message:");
        String message = scanner.next();

        String post = "{" + algorithmText + ":" + taskUrl + ","+
                payloadText + ":" + resource + "," +
                userText + ":" + method + "," +
                jobText + ":" +"{" +
                                taskText +":" + taskUrl+","+
                                resourceText + ":" + resource+","+
                                methodText + ":" + method+","+
                                dataText + ":" + ""+","+
                                callbackText + ":" + callbackUrl+","+
                                messageText  + ":" + message
                                + "}," +
                messageText + ":" + message +"}";

        HttpEntity<String> entity = new HttpEntity<>(post, headers);


        try {

            ResponseEntity<Object> response = restTemplate.exchange(electionUrl, HttpMethod.POST, entity, Object.class);

        } catch (HttpStatusCodeException exception) {

        }
    }



    public void menu() throws Exception {

        String input="";

        while (!input.equals("q")) {

            System.out.println("\n___________________");
            System.out.println("Adventurer menu:");
            System.out.println("1: Play first quest");
            System.out.println("2: Login");
            System.out.println("3: Register");
            System.out.println("4: Hiring");
            System.out.println("5: Assignment");
            System.out.println("6: Election");

            System.out.println("q: Quit");
            input = scanner.next();


            switch (input) {
                case "1":
                    if (loginToken==null) {System.out.println("Please login first.");}
                    else {playFirstQuest();} break;
                case "2": login(); break;
                case "3": register(); break;
                case "4": hiring(); break;
                case "5": assignment(); break;
                case "6": election(); break;
                case "q": break;
            }
        }
        System.out.println("Goodbye!");
    }
}
