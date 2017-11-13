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

                reply.setPerformative(ACLMessage.INFORM);
                try {
                    reply.setContentObject(artifactsList);

                } catch (IOException e) {
                    e.printStackTrace();
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
                        reply.setPerformative(ACLMessage.INFORM);
                        try {
                            reply.setContentObject(infoList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        reply.setPerformative(ACLMessage.FAILURE);
                }
                else{
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                myAgent.send(reply);
            }
            else
            block();
        }
    }
}

