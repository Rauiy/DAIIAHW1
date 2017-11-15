import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.JSONFunctions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProfilerAgent extends Agent
{
    class personalInfo implements Serializable{
        final private int age;
        final private String occupation;
        final private boolean gender;
        final private String interests;

        public personalInfo(int age, String occupation, boolean gender, String interests) {
            this.age = age;
            this.occupation = occupation;
            this.gender = gender;
            this.interests = interests;
        }

        public int getAge() {
            return age;
        }

        public String getOccupation() {
            return occupation;
        }

        public boolean getGender() {
            return gender;
        }

        public String getInterests() {
            return interests;
        }

        @Override
        public String toString() {
            return "personalInfo{" +
                    "age=" + age +
                    ", occupation='" + occupation + '\'' +
                    ", gender=" + gender +
                    ", interests=" + interests +
                    '}';
        }
    }

    private List<Artifact> visited;
    private ArrayList<String> artifactIds;
    private ArrayList<Artifact> artifacts;
    private AID tourAgent;
    private AID curatorAgent;
    private personalInfo pi;
    protected void setup()
    {
        System.out.println("Hello I am "+ getLocalName());


        visited = new ArrayList<Artifact>();

        Object[] args = getArguments();
        int a = 21;
        String o = "student";
        boolean g = true; //True = male, false = female
        String in = "mountains";
        if(args != null){
            int i = 0;
            while(i < args.length){
                String arg = (String) args[i];
                if(arg.equals("-a")){
                    i++;
                    a = Integer.parseInt((String)args[i]);
                }else if(arg.equals("-o")){
                    i++;
                    o = (String)args[i];
                }else if(arg.equals("-g")){
                    i++;
                    g = Boolean.parseBoolean((String)args[i]);
                }else if(arg.equals("-i")){
                    i++;
                    in = (String)args[i];
                }
                i++;
            }
        }

        pi = new personalInfo(a, o, g, in);

        System.out.println("Profiler initiated with info: " + pi.toString());
        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                System.out.println(getLocalName() + ": Trying to get a personalized tour");
                // Update the list of seller agents


                tourAgent = findAgents(myAgent, "Tour-provider");

                if(tourAgent == null) {
                    System.out.println(getLocalName() + ": No tour guide found...");
                    return;
                }

                curatorAgent = findAgents(myAgent, "Artifact-provider");

                if(curatorAgent == null) {
                    System.out.println(getLocalName() + ": No curator found...");
                    return;
                }

                SequentialBehaviour sb = new SequentialBehaviour();

                sb.addSubBehaviour(new RequestPersonalTourProposal());

                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(curatorAgent);
                sb.addSubBehaviour(new RequestArtifactDetails(myAgent,req));
                addBehaviour(sb);
            }
        });
    }

    static public AID findAgents(Agent myAgent, String type) {
        AID tmp = null;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            // System.out.print("Found the following agents: ");
            // Should only exist one agent of each, so take the first one
            if (result.length > 0) {
                tmp = result[0].getName();
                //   System.out.println(tmp);
            } else
                System.out.println("none");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        return tmp;
    }

    class RequestPersonalTourProposal extends OneShotBehaviour{

        @Override
        public void action() {
            //personal tour request(ptr)
            ACLMessage ptr = new ACLMessage(ACLMessage.CFP);
            ptr.addReceiver(tourAgent);
            try {
                ptr.setContentObject(pi);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ptr.setConversationId("Tour-provider");
            ptr.setReplyWith("ptr"+System.currentTimeMillis());

            myAgent.send(ptr);
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Tour-provider"),
                    MessageTemplate.MatchInReplyTo(ptr.getReplyWith()));

            ACLMessage reply = myAgent.blockingReceive(mt);
            if (reply.getPerformative() == ACLMessage.PROPOSE) {
                // This is an offer
                try {
                    artifactIds = (ArrayList<String>) reply.getContentObject();
                    System.out.println(getLocalName() + ": Received proposal: " + artifactIds.toString());
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
            else if(reply.getPerformative() == ACLMessage.FAILURE){
                System.out.println("No artifacts found, try again next tick");
            }
        }
    }

    class RequestArtifactDetails extends SimpleAchieveREInitiator{

        public RequestArtifactDetails(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        @Override
        protected ACLMessage prepareRequest(ACLMessage msg){
            try {
                msg.setContentObject(artifactIds);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return msg;
        }

        @Override
        protected void handleInform(ACLMessage msg){
            try {
                artifacts = (ArrayList<Artifact>) msg.getContentObject();
                for(Artifact a: artifacts)
                    visited.add(a);
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
    }
    /*
    class TourRequester extends Behaviour {
        int step = 0;
        private MessageTemplate mt; // The template to receive replies
        private MessageTemplate mt2; // The template to receive replies
        ArrayList<String> artifactIds;
        ArrayList<Artifact> artifacts;
        int replyCount = 0;
        TourRequester(){}

        @Override
        public void action() {
            switch (step){
                case 0:
                    System.out.println(getLocalName() + ": Call for tour proposal");
                    // Call for a tour proposal
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

                    try {
                        cfp.setContentObject(pi);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    cfp.addReceiver(tourAgent);
                    replyCount = 0;

                    cfp.setConversationId("Tour-provider");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Tour-provider"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive the tour proposal
                    System.out.println(getLocalName() + ": Waiting for msg");
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received

                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            try {
                                artifactIds = (ArrayList<String>) reply.getContentObject();
                                System.out.println(getLocalName() + ": Received proposal: " + artifactIds.toString());
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                            step = 2;
                        }
                        else if(reply.getPerformative() == ACLMessage.FAILURE){
                            System.out.println("No artifacts found, try again later");
                            step = 5;
                        }
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    // Request for artifacts
                    System.out.println(getLocalName() + ": Request proposed artifacts");
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST_WHEN);

                    try {
                        request.setContentObject(artifactIds);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    request.addReceiver(curatorAgent);

                    request.setConversationId("Artifact-provider");
                    request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
                    myAgent.send(request);
                    mt2 = MessageTemplate.and(MessageTemplate.MatchConversationId("Artifact-provider"),
                            MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    // Receive artifacts
                    System.out.println(getLocalName() + ": waiting for artifact info");
                    ACLMessage artRep = myAgent.receive(mt2);
                    if (artRep != null) {
                        // Reply received

                        if (artRep.getPerformative() == ACLMessage.CONFIRM) {
                            // This is an offer

                            try {
                                artifacts = (ArrayList<Artifact>) artRep.getContentObject();
                                System.out.println("Received artifact info: " + artifacts.toString());
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }
                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
                case 4:
                    // Add artifacts to visited
                    System.out.println(getLocalName() + ": Adding to visited");
                    visited.addAll(artifacts);

                    System.out.println(getLocalName() + ": Visited list");
                    for(Artifact a: visited){
                        System.out.println(a);
                    }
                    step = 5;
                    break;
            }
        }

        @Override
        public boolean done() {
            return (step == 5);
        }
    }
    */

}