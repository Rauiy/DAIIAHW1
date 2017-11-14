import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

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
        addBehaviour(new myBehaviour());

    }

    private class myBehaviour extends CyclicBehaviour {
        private ProfilerAgent.personalInfo personalInfo;
        private MessageTemplate mt;
        private Artifact artifact;
        private int step = 0;
        @Override

        public void action() {
            switch (step) {
                case 0:
                    MessageTemplate mtt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                    ACLMessage msg = myAgent.receive(mtt);
                    if(msg != null) {
                        try {
                            personalInfo = (ProfilerAgent.personalInfo) msg.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    step = 1;
                    break;
                case 1:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.setConversationId("Artifacts-propose");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis());
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Artifacts-propose"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 2;
                    break;
                case 2:
                    //Receive reply from Curator
                    ACLMessage reply = myAgent.receive(mt);
                    step = 3;
                    break;
                case 3:
                    switch (personalInfo.getAge()/10) {
                        case 0:
                            System.out.println("Age 0-9");
                            break;
                        case 1:
                            System.out.println("Age 10-19");
                            break;
                        case 2:
                            System.out.println("Age 20-29");
                            break;
                        case 3:
                            System.out.println("Age 30-39");
                            break;
                        case 4:
                            System.out.println("Age 40-49");
                            break;
                    }
                    //Reply Virtual tour to Profiler
                    break;
            }
        }
    }

}
