package Server;

import Common.Card;
import Common.Project;
import Exceptions.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ProjectsDatabase implements Serializable {
    private final HashMap<String, Project> projects;

    public ProjectsDatabase(){
        projects = Storage.restoreProjects();

    }

    public String addProject(String projectName, String user,MultiGenerator multiGenerator) throws NullPointerException, ProjectAlreadyAddedException {
        if (projectName == null || user == null || multiGenerator == null) throw new NullPointerException();
        String multiIp = multiGenerator.generateIp();
        Project project = new Project(projectName,user,multiIp);
        synchronized (projects) {
            if(projects.putIfAbsent(projectName,project) != null) {
                multiGenerator.addReusedIp(multiIp); //il progetto esiste già, l'indirizzo appena generato posso riusarlo
                throw new ProjectAlreadyAddedException();
            }
            else {
                Storage.writeProjectInfo(project);
                Storage.writeLastMultiAddress(multiGenerator); //salvo l'ultimo indirizzo ip dal quale ripartire una volta spento il server
            }
        }
        return multiIp;

    }

    public void addCard(String projectName, String cardName, String description, String user) throws CardAlreadyExistException, ProjectNotFoundException, NotPermittedException {
        if(projectName == null || cardName == null || description == null)
            throw new NullPointerException();

        synchronized (projects) {
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            if (!project.getUsers().contains(user)) throw new NotPermittedException();

            Card card = new Card(cardName,description);
            if(project.addCard(card)) {
                Storage.writeCardToJson(card, projectName);
            }

            else
                throw new CardAlreadyExistException();

        }

    }

    public void moveCard(String projectName,String cardName,String listaPartenza,String listaDestinazione,String userLogged) throws NullPointerException, ProjectNotFoundException, NotPermittedException, IllegalArgumentException, ListMisMatchException, CardNotFoundException {
        if(projectName == null || cardName == null || listaDestinazione == null || listaPartenza == null || userLogged == null)
            throw new NullPointerException();
        synchronized (projects){
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            if (!project.getUsers().contains(userLogged)) throw new NotPermittedException();


            if(!project.listExists(listaPartenza) || !project.listExists(listaDestinazione))
                throw new IllegalArgumentException();

            Card card = project.moveCard(cardName,listaPartenza,listaDestinazione);
            Storage.writeCardToJson(card,projectName);


        }
    }


    public void removeProject(String projectName, String userLogged, MultiGenerator multiGenerator,String ip) throws NullPointerException,ProjectNotFoundException,NotPermittedException, NotAllDoneException{
        if(projectName == null || userLogged == null || multiGenerator == null || ip == null) throw new NullPointerException();
        synchronized (projects){
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            if (!project.getUsers().contains(userLogged)) throw new NotPermittedException();
            if(!project.allCardsDone()){
                System.out.println("eccezione");
                throw new NotAllDoneException();
            }


            multiGenerator.addReusedIp(ip);
            projects.remove(projectName);
            Storage.deleteProjectDirectory(projectName);
            Storage.writeLastMultiAddress(multiGenerator);
        }

    }

    public ArrayList<String> getCardsProject(String projectName, String userLogged) throws NullPointerException,ProjectNotFoundException,NotPermittedException,NoCardsExceptions{
        ArrayList<String> list;
        if(projectName == null || userLogged == null)
            throw new NullPointerException();

        synchronized (projects) {
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            if (!project.getUsers().contains(userLogged)) throw new NotPermittedException();

            list = project.getCardsList();
            if (list.isEmpty()) throw new NoCardsExceptions();
        }
            return list;

    }

    public String getCardInfo(String projectName, String cardName, String userLogged) throws NullPointerException,ProjectNotFoundException,CardNotFoundException,NotPermittedException{
        Card card;
        if(projectName == null || cardName == null || userLogged == null)
            throw new NullPointerException();
        synchronized (projects) {
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            if (!project.getUsers().contains(userLogged)) throw new NotPermittedException();

            card = project.findCard(cardName);
            if(card == null) throw new CardNotFoundException();

            return "Nome: " + cardName + "\nDescrizione: " + card.getDescription() + "\nStato: " + card.getLastState();


        }

    }

    public ArrayList<String> getProjectHistory(String projectName, String cardName, String userLogged) throws NullPointerException, ProjectNotFoundException, NotPermittedException, CardNotFoundException {
        if(projectName == null || cardName == null || userLogged == null)
            throw new NullPointerException();

        synchronized (projects){
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            if (!project.getUsers().contains(userLogged)) throw new NotPermittedException();

            Card card = project.findCard(cardName);
            if(card == null) throw new CardNotFoundException();

            return card.getHistory(); // l'history ha sempre almeno il primo stato, non può essere null se la card esiste

        }
    }


    public ArrayList<String> getMembersProject(String projectName,String userLogged) throws NullPointerException, ProjectNotFoundException, NotPermittedException {
        if(projectName == null || userLogged == null)
            throw new NullPointerException();

        synchronized (projects){
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();

            if (!project.getUsers().contains(userLogged)) throw new NotPermittedException();

            return project.getUsers();
        }
    }


    public Project getProject(String projectName) throws NullPointerException, ProjectNotFoundException {
        if(projectName == null)
            throw  new NullPointerException();

        synchronized (projects){
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            return project;
        }
    }

    public boolean userInProject(String projectName, String user) throws NullPointerException, ProjectNotFoundException {
        if(projectName == null || user == null) throw new NullPointerException();
        synchronized (projects){
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            if(project.getUsers().contains(user))
                return true;
        }
        return false;
    }

    public String getProjectIp(String projectName) throws NullPointerException{
        if(projectName == null)
            throw  new NullPointerException();

        synchronized (projects){
            Project project = projects.get(projectName);
            if(project != null)
                return projects.get(projectName).getMultiAddress();

        }
        return null;

    }

    public ArrayList<String> getListOfProjects(String user) throws NullPointerException, NoProjectsException{
        if(user == null) throw new NullPointerException();
        ArrayList<Project> list = new ArrayList<>(projects.values());
        ArrayList<String> userProjects = new ArrayList<>();
        for(Project project : list){
            if(project.getUsers().contains(user)){
                userProjects.add(project.getProjectName());
            }

        }

        if(userProjects.isEmpty())
            throw new NoProjectsException();

        return userProjects;
    }

    public Project addMember(String projectName, String nickName,String userLoggedIn) throws NullPointerException,ProjectNotFoundException,UserAlreadyAddedException,NotPermittedException{
        if(projectName == null || nickName == null || userLoggedIn == null) throw new NullPointerException();
        synchronized (projects) {
            Project project = projects.get(projectName);
            if (project == null) throw new ProjectNotFoundException();
            if (!project.getUsers().contains(userLoggedIn)) throw new NotPermittedException();

            if(project.getUsers().contains(nickName)) //se l'utente da aggiungere è già presente nel progetto
                throw new UserAlreadyAddedException();

            project.addMember(nickName);
            Storage.writeProjectInfo(project); //aggiorno il file json per ricostruire il Db dei progetti

            return project;
        }

    }


}
