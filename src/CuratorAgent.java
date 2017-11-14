import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
    private HashMap<String,Artifacts> artifactsList;

    protected void setup(){
        System.out.println("Curator Agent initializing");
        // Initiate Artifactlist

        // Initiate Behaviours/Servers
    }

    private class ArtifactRequestServer extends CyclicBehaviour{

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = this.myAgent.receive(mt);
            if(msg != null){
                ACLMessage reply = msg.createReply();


                try {
                    reply.setContentObject(artifactsList);
                    reply.setPerformative(ACLMessage.PROPOSE);
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

    private class SpecificArtifactRequestServer extends CyclicBehaviour{

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = this.myAgent.receive(mt);
            if(msg != null){
                String content = msg.getContent();
                ArrayList<Artifacts> infoList = new ArrayList();
                String[] artifacts = content.split(",");
                ACLMessage reply = msg.createReply();

                if(artifacts.length > 0) {
                    for (String s : artifacts) {
                        Artifacts tmp = artifactsList.get(s);
                        if(tmp != null)
                            infoList.add(tmp);
                    }

                    if(infoList.size() != 0) {
                        try {
                            reply.setContentObject(infoList);
                            reply.setPerformative(ACLMessage.INFORM);
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

