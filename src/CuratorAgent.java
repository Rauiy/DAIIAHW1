
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.introspection.AddedBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Steven on 2017-11-13.
 */
public class CuratorAgent extends Agent {
    private HashMap<String,Artifact> artifactsList;
    // The GUI by means of which the user can add books in the catalogue
    private CuratorGui myGui;
    protected void setup(){
        System.out.println("Curator Agent initializing");
        // Initiate ArtifactList

        artifactsList = new HashMap<String, Artifact>();

        // Create and show the GUI
        myGui = new CuratorGui(this);
        myGui.showGui();

        // Register the curator service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Artifact-provider");
        sd.setName("JADE-museum");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Initiate proposal server
        addBehaviour(new ArtifactProposeServer());

        // Initiate request handler rerver
        addBehaviour(new ArtifactRequestHandlerServer());
    }

    public void updateArtifacts(String name, String creator, int date, String location, String genre){
        String id = name+creator+date;
        artifactsList.put(id, new Artifact(id,name, creator, date,location,genre));
        System.out.println(artifactsList.toString());
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Close the GUI
        myGui.dispose();
        // Printout a dismissal message
        System.out.println("Seller-agent "+getAID().getName()+" terminating.");
    }


    private class ArtifactProposeServer extends CyclicBehaviour{

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                ACLMessage reply = msg.createReply();
                System.out.println("Received a request for all artifacts");
                try {
                    reply.setContentObject(new ArrayList<Artifact>(artifactsList.values()));
                    reply.setPerformative(ACLMessage.CONFIRM);
                } catch (IOException e) {
                    e.printStackTrace();
                    reply.setContent("Failed to serialize list of artifacts");
                    reply.setPerformative(ACLMessage.FAILURE);
                }

                myAgent.send(reply);
            }
            else
                block();
        }
    }

    private class ArtifactRequestHandlerServer extends CyclicBehaviour{
        ArrayList<String> artifactIds;
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHEN);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                try {
                    artifactIds =(ArrayList<String>) msg.getContentObject();
                    if(artifactIds == null || artifactIds.size() <= 0){
                            return;
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                ArrayList<Artifact> infoList = new ArrayList();
                ACLMessage reply = msg.createReply();

                if(artifactIds.size() > 0) {
                    System.out.println("Received a request for: " + artifactIds.toString());
                    for (String s : artifactIds) {
                        Artifact tmp = artifactsList.get(s);
                        if(tmp != null) {
                            infoList.add(tmp);
                        }
                    }

                    if(infoList.size() != 0) {
                        try {
                            reply.setContentObject(infoList);
                            reply.setPerformative(ACLMessage.CONFIRM);
                            System.out.println("Artifacts found");
                        } catch (IOException e) {
                            e.printStackTrace();
                            reply.setContent("Failed to serialize list of artifacts");
                            reply.setPerformative(ACLMessage.FAILURE);
                        }
                    }
                    else{
                        reply.setContent("Failed to find any matching artifact");
                        reply.setPerformative(ACLMessage.FAILURE);
                    }
                }
                else{
                    reply.setContent("No artifact was requested");
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                myAgent.send(reply);
            }
            else
            block();
        }
    }
}

