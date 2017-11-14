import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
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
public class TourAgent extends Agent {
    private HashMap<Integer,List<Artifact>> tours;
    private AID curator;

    protected void setup() {
        System.out.println("Hallo, " + getLocalName() + " in tha house");

        // Register the tour guide service in the yellow pages
        DFAgentDescription template = new DFAgentDescription();
        template.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Tour-provider");
        sd.setName("JADE-museum-platform");
        template.addServices(sd);
        try {
            DFService.register(this, template);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        template = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType("Artifact-Provider");
        template.addServices(sd);

        curator = ProfilerAgent.findAgents(this, template);

        if(curator == null) {
            System.out.println("No curator found...");
            return;
        }

        addBehaviour(new InterfaceServer());
    }

    private class InterfaceServer extends CyclicBehaviour {
        private ProfilerAgent.personalInfo personalInfo;
        private MessageTemplate mt;
        ArrayList<Artifact> artifacts;
        ArrayList<String> artifactIds;
        private int step = 0;
        private ACLMessage requestReply;
        @Override

        public void action() {
            switch (step) {
                case 0:
                    MessageTemplate mtt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    ACLMessage msg = myAgent.receive(mtt);
                    if(msg != null) {
                        try {
                            personalInfo = (ProfilerAgent.personalInfo) msg.getContentObject();
                            System.out.println("Call for proposal received");
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        requestReply = msg.createReply();
                        step = 1;
                    }
                    else{
                        block();
                    }

                    break;
                case 1:
                    System.out.println("Sending request for artifacts");
                    ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);

                    cfp.addReceiver(curator);
                    cfp.setConversationId("Artifact-provider");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis());

                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Artifact-provider"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 2;

                    break;
                case 2:
                    //Receive reply from Curator
                    ACLMessage reply = myAgent.receive(mt);
                    if(reply != null){
                        if (reply.getPerformative() == ACLMessage.CONFIRM) {
                            // This is an offer

                            try {
                                artifacts = (ArrayList<Artifact>) reply.getContentObject();
                                System.out.println("Artifacts received");
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }
                        step = 3;
                    }
                    else{
                        block();
                    }
                    break;
                case 3:
                    System.out.println("Creating a personalized tour");
                    int century = 0;
                    switch (personalInfo.getAge()/10) {
                        case 0:
                            System.out.println("Age 0-9");
                            century = 2000;
                            break;
                        case 1:
                            System.out.println("Age 10-19");
                            century = 1900;
                            break;
                        case 2:
                            System.out.println("Age 20-29");
                            century = 1800;
                            break;
                        case 3:
                            System.out.println("Age 30-39");
                            century = 1700;
                            break;
                        case 4:
                        default:
                            System.out.println("Age 40+");
                            century = 1600;
                            break;
                    }
                    artifactIds = new ArrayList<String>();
                    for(Artifact a: artifacts){
                        if(a.getCenturyOfCreation() >= century && a.getCenturyOfCreation() < century+100){
                            artifactIds.add(a.getId());
                        }
                    }
                    step = 4;
                    break;
                case 4:
                    System.out.println("Proposing the personalized tour");
                    if(artifactIds.size() > 0) {
                        try {
                            requestReply.setContentObject(artifactIds);
                            requestReply.setPerformative(ACLMessage.PROPOSE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            requestReply.setPerformative(ACLMessage.FAILURE);
                            requestReply.setContent("Failed to serialize the tour");
                        }

                    }else{
                        requestReply.setPerformative(ACLMessage.FAILURE);
                        requestReply.setContent("Failed to build a tour");
                    }
                    myAgent.send(requestReply);
                    step = 0;
                    break;
            }
        }
    }

}
