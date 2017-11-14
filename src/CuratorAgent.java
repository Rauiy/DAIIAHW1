
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.introspection.AddedBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREResponder;

import java.io.IOException;
import java.util.*;

/**
 * Created by Steven on 2017-11-13.
 */
public class CuratorAgent extends Agent {
    private HashMap<String,Artifact> artifactsList;
    // The GUI by means of which the user can add books in the catalogue
    private CuratorGui myGui;
    private String[] genres = {"mountains","flowers","animals","lakes","plants","environment","cats","dogs"};
    protected void setup(){
        System.out.println("Curator Agent initializing");
        // Initiate ArtifactList

        artifactsList = new HashMap<String, Artifact>();

        SequentialBehaviour sb = new SequentialBehaviour();
        sb.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                Random r = new Random();
                for(int i = 0; i < 100; i++){

                    updateArtifacts("name" + i, "creator" + i, r.nextInt(2000), "city"+i,genres[r.nextInt(genres.length)]);
                }
            }
        });

        // Create and show the GUI
        myGui = new CuratorGui(this);
        myGui.showGui();

        registerAtDF();
    }

    public void registerAtDF(){
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
    }

    public void updateArtifacts(String name, String creator, int date, String location, String genre){
        String id = name+creator+date;
        artifactsList.put(id, new Artifact(id,name, creator, date,location,genre));
        //System.out.println(artifactsList.toString());
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


    private class ArtifactRequestHandler extends SimpleAchieveREResponder{
        public ArtifactRequestHandler(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.PROPOSE);
            try {
                reply.setContentObject(new ArrayList<String>(Arrays.asList(genres)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return reply;
        }
    }

    private class DetailedArtifactRequestHandler extends SimpleAchieveREResponder{

        public DetailedArtifactRequestHandler(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
            ArrayList<String> artifactIds = null;

            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.PROPOSE);
            try {
                artifactIds = (ArrayList<String>) request.getContentObject();
                ArrayList<Artifact> detailedList = new ArrayList();
                for(String id: artifactIds){
                    Artifact tmp = artifactsList.get(id);
                    if(tmp != null)
                        detailedList.add(tmp);
                }
                reply.setContentObject(detailedList);
            } catch (UnreadableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return reply;
        }
    }

    /*
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
    }*/
}

