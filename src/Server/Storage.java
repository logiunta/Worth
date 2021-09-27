package Server;

import Common.Card;
import Common.Project;
import Common.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Storage {
    private static final String firstMultiIp = "224.0.0.255";
    private static final File dataDir = new File("./data");
    private static final File usersDir = new File("./data/UsersDb");
    private static final File usersFile = new File("./data/UsersDb/WorthUsers.json");
    private static final File projectsDir = new File("./data/Projects");
    private static final File multiAddressFile = new File("./data/LastMultiAddress.json");
    private static MultiGenerator multiGenerator;
    private static ArrayList<Project> listOfProjects;
    private static ArrayList<User> listOfUsers;


    public static HashMap<String, User> restoreUsers() {
        if(!dataDir.exists()){
            dataDir.mkdir();
        }
        if (!usersDir.exists()) {
            usersDir.mkdir();
            try {
                usersFile.createNewFile();
            } catch (IOException e) {
                System.out.println("Errore nella creazione del file json");
            }

        } else {
            if (!usersFile.exists()) {
                try {
                    usersFile.createNewFile();
                } catch (IOException e) {
                    System.err.println("Errore nella creazione del file json");
                }
            }
        }
        HashMap<String, User> users = new HashMap<>();
        listOfUsers = new ArrayList<>();
        fromJsonTolistOfUsers(); //carico la listOfUsersa dei nickNames dal file json

        for (User user : listOfUsers) {                  //carico la listOfUsersa di utenti iscritti dal file json
            users.put(user.getNickName(), user);

        }

        AllOffline(); //resetta tutti gli stati in offline

        return users;

    }

    private static void AllOffline() {
        Iterator<User> iterator = listOfUsers.iterator();
        while (iterator.hasNext()) {
            User u = iterator.next();
            u.setStatus("offline");

        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(usersFile, listOfUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static MultiGenerator restoreLastMultiAddress(){
        if(!dataDir.exists()){
            dataDir.mkdir();
        }
        if(!multiAddressFile.exists()){
            try {
                multiAddressFile.createNewFile();
                multiGenerator = new MultiGenerator(firstMultiIp,new ArrayList<>());
                writeLastMultiAddress(multiGenerator);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else multiGenerator = new MultiGenerator(firstMultiIp,new ArrayList<>());
        fromJsonToMultiAddress();

        return multiGenerator;

    }


    public static HashMap<String,Project> restoreProjects() {
        if(!dataDir.exists()){
            dataDir.mkdir();
        }
        if (!projectsDir.exists()) {
            projectsDir.mkdir();
        }

        listOfProjects = new ArrayList<>();

        for (File dir : projectsDir.listFiles()) {
            if (dir.isDirectory()) {
                Project project = getProjectInfo(dir);
                listOfProjects.add(project);
                File cardsDir = dir.listFiles(File::isDirectory)[0];
                fromJsonTolistOfCards(cardsDir, project);

            }

        }
        HashMap<String,Project> projects = new HashMap<>();

        for(Project project : listOfProjects)
            projects.put(project.getProjectName(),project);

        return projects;

    }

    public static void writeProjectInfo(Project project){
        ObjectMapper objectMapper = new ObjectMapper();
        File projectFile = new File(projectsDir + "/" + project.getProjectName() + "/" + project.getProjectName() + "Info.json");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(projectFile,project);

        } catch (IOException e) {
            System.err.println("File non trovato");
        }

    }


    public static void writeUsersToJson(ArrayList<User> list) {
        listOfUsers = list;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(usersFile, listOfUsers);

        } catch (IOException e) {
            System.err.println("File non trovato");
        }

    }

    public static void deleteProjectDirectory(String projectName){
        File projectDir = new File(projectsDir + "/" + projectName);
        recDeleteFiles(projectDir);

    }
    private static void recDeleteFiles(File projectsDir){
        File[] list = projectsDir.listFiles();
        if(list == null) return;

        for(File file : list){
            if(file.isDirectory()){
                recDeleteFiles(file);
            }
            else file.delete();
        }
        projectsDir.delete();
    }

    public static void writeLastMultiAddress(MultiGenerator multiGenerator){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(multiAddressFile,multiGenerator);
        }
        catch (IOException e){
            System.out.println("File non trovato");
        }
    }

    public static void writeCardToJson(Card card, Project project){
        File cardsDir = new File(projectsDir + "/" + project.getProjectName() + "/Cards");
        File jsonFile = new File(projectsDir + "/" + project.getProjectName() + "/Cards" + "/" + card.getName() +".json");

        if(!cardsDir.exists()){
            cardsDir.mkdir();
            if(!jsonFile.exists())
                try {
                    jsonFile.createNewFile();

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile,card);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Project getProjectInfo(File dir) {
        String name = dir.getName();
        Project project = null;
        File file = new File("./data/Projects/" + name + "/" + name + "Info.json");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            project = objectMapper.readValue(file,Project.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

       return project;
}


    private static void fromJsonTolistOfCards(File cardsDir, Project project) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            for (File cardJson : cardsDir.listFiles()) {
                Card card = objectMapper.readValue(cardJson,Card.class);
                project.restoreCard(card);

            }
        } catch (FileNotFoundException e) {
            System.err.println("File non trovato");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void fromJsonToMultiAddress() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            MultiGenerator node = objectMapper.readValue(multiAddressFile,multiGenerator.getClass());
            if(node != null)
                multiGenerator = node;


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void fromJsonTolistOfUsers() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode tree = objectMapper.readTree(usersFile);
            if (tree == null) return;

            if (tree.isArray()) {
                int cont = 0;
                for (JsonNode node : tree) {
                    node = tree.get(cont);
                    User u = objectMapper.treeToValue(node, User.class);
                    listOfUsers.add(u);
                    cont++;
                }
            } else {
                User u = objectMapper.treeToValue(tree, User.class);
                listOfUsers.add(u);
            }


        } catch (FileNotFoundException e) {
            System.err.println("File non trovato");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
