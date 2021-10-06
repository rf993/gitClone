/*
 * Copyright Public Record Office Victoria 2021
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 */
package VERSCommon;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class captures and prepares a summary of messages. Essentially, it
 * groups messages that have a common prefix, recording the identifiers of the
 * objects that generated the messages.
 *
 * The messages are represented as a tree of LetterMatch structures radiating
 * from a dummyRoot. Each LetterMatch structure represents a character in the
 * common prefix; the path from the root to a node spells out the common prefix
 * to that node.
 *
 * If the common prefix is an instance of the entire message, then the message
 * is placed in the 'matchedMesg' field of the node. If the instances have text
 * after the common prefix, the messages are placed in the 'tailsOfMessage'
 * list. Any node can have both a 'matchedMesg' and a 'tailsOfMessage' list.
 *
 * @author Andrew
 */
public class ResultSummary {

    LetterMatch dummyRoot;      // stored results
    String currentId;           // current identifier to be used to label new results

    /**
     * Creates a ResultSummary
     */
    public ResultSummary() {
        dummyRoot = new LetterMatch();
        currentId = "unknown";
    }

    /**
     * Free a result summary and everything in it
     */
    public void free() {
        dummyRoot.free();
    }

    /**
     * Set the id to be used for subsequent recording results
     *
     * @param id
     */
    public void setId(String id) {
        currentId = id;
    }

    /**
     * Add a new message to the result structure. Uses the 'currentId' (set by a
     * previous call to 'setId()') as the identifier for the source of this
     * message. The message is assumed to be an ERROR.
     *
     * @param type The type of the message
     * @param mesg Message to be recorded
     */
    /*
    public void recordResult(Type type, String mesg) {
        recordResult(type, mesg, currentId, null);
    }
     */
    /**
     * Add a new message to the result structure. The message has a type, and an
     * identifier that identifies the source of the message. The identifier is
     * formed from an id that identifies a discrete object, and a subId that
     * identifies a component within the object. If the id is null, the
     * currentId is used (set with setId). If the subId is null, it is ignored.
     *
     * @param type is the message an error or warning?
     * @param mesg the message being added
     * @param id an identifier associated with *this* instance of the message
     * @param subId identifies the part in the instance
     */
    public void recordResult(Type type, String mesg, String id, String subId) {
        mesg = mesg.replaceAll("\r", " ");
        mesg = mesg.replaceAll("\n", " ");
        mesg = mesg.replaceAll("\t", " ");
        mesg = mesg.replaceAll("   ", " ");
        mesg = mesg.replaceAll("  ", " ");
        if (id == null) {
            id = currentId;
        }
        dummyRoot.addMesg(type, mesg, 0, id, subId);
    }

    public void report(Writer w) throws IOException {
        SimpleDateFormat sdf;
        TimeZone tz;

        w.append("\r\n");
        w.append("********************************************************************************\r\n");
        w.append("*                                                                              *\r\n");
        w.append("* SUMMARY REPORT OF ERRORS & WARNINGS                                          *\r\n");
        w.append("*                                                                              *\r\n");
        w.append("********************************************************************************\r\n");
        w.append("\r\n");
        w.write("Test run: ");
        tz = TimeZone.getTimeZone("GMT+10:00");
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+10:00");
        sdf.setTimeZone(tz);
        w.write(sdf.format(new Date()));
        w.write("\r\n");
        w.write("\r\n");
        w.write("ERRORS THAT OCCURRED:\r\n");
        dummyRoot.report(Type.ERROR, w);
        w.write("\r\n");
        w.write("WARNINGS THAT OCCURRED:\r\n");
        dummyRoot.report(Type.WARNING, w);
        w.write("\r\n");
        w.flush();
    }

    /**
     * Dump the result structure for diagnostic purposes
     *
     * @return
     */
    @Override
    public String toString() {
        return dummyRoot.toString(0);

    }

    /**
     * What type of message is this?
     */
    public enum Type {
        WARNING, // this message is just a warning & doesn't need to be fixed
        ERROR;      // this message is an error & does need to be fixed

        static public int length = Type.values().length; // number of values of type

        static public String toString(int i) {
            switch (i) {
                case 0:
                    return "WARNING";
                case 1:
                    return "ERROR";
                default:
                    return "UNKNONW";
            }
        }
    }

    /**
     * Internal class that represents a common character in the stored messages.
     * The stored messages are represented as a tree of LetterMatch element,
     * rooted at a dummy node. The path from the root to any node spells out the
     * common prefix to that node.
     */
    private class LetterMatch {

        private LetterMatch next, prev; // implements a doubly linked list
        char letter;                    // letter in the prefix
        LetterMatch nextLetters;        // doubly linked list of the next characters in the prefix
        ArrayList<Mesg> tailsOfMessage; // any non common tails of this prefix
        Mesg matchedMesg;               // the message if the prefix to this point is the entire message
        boolean dummyRoot;              // true if a dummy root

        /**
         * create a node in the result structure tree identified by the
         * specified character
         *
         * @param ch the character distinguishing this node
         */
        public LetterMatch(char ch) {
            next = this;
            prev = this;
            dummyRoot = false;
            letter = ch;
            nextLetters = null;
            tailsOfMessage = new ArrayList<>();
            matchedMesg = null;
        }

        /**
         * Create the dummy root
         */
        public LetterMatch() {
            next = this;
            prev = this;
            dummyRoot = true;
            letter = ' ';
            nextLetters = null;
            tailsOfMessage = new ArrayList<>();
            matchedMesg = null;
        }

        /**
         * Free the node and all contained in it (including subordinate nodes)
         */
        public void free() {
            tailsOfMessage.clear();
            tailsOfMessage = null;
            matchedMesg = null;
            if (nextLetters != null) {
                nextLetters.free();
            }
            nextLetters = null;
            prev.next = null;
            prev = null;
            if (next != null) {
                next.free();
            }
            next = null;
        }

        /**
         * Dump the contents of the node as a string
         *
         * @param depth amount of indent
         * @return the string
         */
        public String toString(int depth) {
            StringBuilder sb = new StringBuilder();
            int i, j;
            LetterMatch lm;

            for (i = 0; i < depth; i++) {
                sb.append(' ');
            }
            sb.append("lm ");
            if (dummyRoot) {
                sb.append("root ");
            } else {
                sb.append("c='");
                sb.append(letter);
                sb.append("' ");
            }
            sb.append("ms={");
            if (matchedMesg != null) {
                sb.append(matchedMesg.toString());
            } else {
                sb.append("No mesg");
            }
            sb.append("} {\n");
            if (tailsOfMessage.size() > 0) {
                for (j = 0; j < tailsOfMessage.size(); j++) {
                    for (i = 0; i < depth + 1; i++) {
                        sb.append(' ');
                    }
                    sb.append(tailsOfMessage.get(j).toString());
                    if (i == depth) {
                        sb.append("}");
                    }
                    sb.append("\n");
                }
            } else {
                for (i = 0; i < depth + 1; i++) {
                    sb.append(' ');
                }
                sb.append("no message tails\n");
            }
            lm = nextLetters;
            if (lm == null) {
                for (i = 0; i < depth + 1; i++) {
                    sb.append(' ');
                }
                sb.append("no next letters\n");
            } else {
                do {
                    sb.append(lm.toString(depth + 1));
                    lm = lm.next;
                } while (lm != nextLetters);
            }
            return sb.toString();
        }

        /**
         * Add a message to the store. This is a recursive routine, starting
         * with the dummy node at the root.
         *
         * @param type is it an ERROR or WARNING message?
         * @param id a unique identifier
         * @param mesg the message to add
         * @param indexCharToMatch the index of the character in the message we
         * are currently adding
         */
        private void addMesg(Type type, String mesg, int indexCharToMatch, String id, String subId) {
            char ch;
            LetterMatch lm, lm1;
            Mesg m, m1;
            int i;

            // if the new message has been consumed, add it as the
            // matched message (if one doesn't exist), otherwise we
            // have found a new version of the matched message
            if (indexCharToMatch >= mesg.length()) {
                if (matchedMesg == null) {
                    matchedMesg = new Mesg(type, mesg, indexCharToMatch - 1, id, subId);
                } else {
                    matchedMesg.addId(type, id, subId);
                }
                return;
            }

            // go through list of letters looking for one that matches the indexCharToMatch'th character of mesg
            ch = mesg.charAt(indexCharToMatch);
            lm = nextLetters;
            if (lm != null) {
                do {
                    if (lm.letter == ch) {
                        break;
                    }
                    lm = lm.next;
                } while (lm != nextLetters);

                // if we found one, recurse into the node, moving to the next letter
                if (lm.letter == ch) {
                    lm.addMesg(type, mesg, indexCharToMatch + 1, id, subId);
                    return;
                }
            }

            // no? go through list of tails
            if (tailsOfMessage.size() > 0) {

                // look for one that starts with the indexCharToMatch'th character of mesg
                for (i = 0; i < tailsOfMessage.size(); i++) {
                    m = tailsOfMessage.get(i);
                    if (m.mesg.charAt(indexCharToMatch) == ch) {
                        break;
                    }
                }

                // if we found one...
                if (i != tailsOfMessage.size()) {

                    // remove matching message from tails
                    m1 = tailsOfMessage.remove(i);

                    // create new next letter match & link it in (in alphabetical order)
                    lm = new LetterMatch(ch);
                    if (nextLetters == null) {
                        nextLetters = lm;
                    } else {
                        lm1 = nextLetters;
                        while (lm1.letter < lm.letter && lm1.next != nextLetters) {
                            lm1 = lm1.next;
                        }
                        if (lm1.letter >= lm.letter) {
                            lm.prev = lm1.prev;
                            lm.prev.next = lm;
                            lm1.prev = lm;
                            lm.next = lm1;
                            if (lm1 == nextLetters) {
                                nextLetters = lm;
                            }
                        } else {
                            lm.prev = nextLetters.prev;
                            lm.prev.next = lm;
                            nextLetters.prev = lm;
                            lm.next = nextLetters;
                        }
                    }

                    // if we have consumed the entire original matching message
                    // put it in the matched message, otherwise put it in the
                    // tail messages
                    if (m1.index == m1.mesg.length()) {
                        lm.matchedMesg = m1;
                    } else {
                        m1.index++;
                        lm.tailsOfMessage.add(m1);
                    }

                    // see if the new message can be matched against
                    // the new letter we have added
                    lm.addMesg(type, mesg, indexCharToMatch + 1, id, subId);
                    return;
                }
            }

            // no? add this message into tails of Message with count of 1
            tailsOfMessage.add(new Mesg(type, mesg, indexCharToMatch + 1, id, subId));
        }

        public void report(Type type, Writer w) throws IOException {
            int j;
            LetterMatch lm;

            // report on messages that completely match the prefix
            if (matchedMesg != null) {
                matchedMesg.report(type, w);
            }

            // report on messages for which this was a prefix
            for (j = 0; j < tailsOfMessage.size(); j++) {
                tailsOfMessage.get(j).report(type, w);
            }

            // recurse for longer prefixes
            lm = nextLetters;
            if (lm != null) {
                do {
                    lm.report(type, w);
                    lm = lm.next;
                } while (lm != nextLetters);
            }
        }
    }

    /**
     * Internal class representing a stored message. This is linked to by the
     * LetterMatch tree.
     */
    private class Mesg {

        Type type;      // the type of the message (Error or Warning)
        String mesg;    // the complete message (including prefix)
        int index;      // the index of the first character *after* the common prefix
        ArrayList<ArrayList<IdRef>> ids; // references to the objects containing this message

        /**
         * Create a new message instance. The message instance has a type and an
         * identifier to the instance that created the message.
         *
         * @param type the type of the message
         * @param mesg the message itself
         * @param index the first character *after* the common prefix
         * @param id the identifier identifying the instance
         * @param subId a subidentifier identifying the position in the instance
         */
        public Mesg(Type type, String mesg, int index, String id, String subId) {
            int i;

            this.type = type;
            this.mesg = mesg;
            this.index = index;
            ids = new ArrayList<>(Type.length);
            for (i = 0; i < Type.length; i++) {
                ids.add(i, new ArrayList<>());
            }
            i = type.ordinal();
            ids.get(i).add(new IdRef(id, subId));
        }

        /**
         * Free the storage for this message
         */
        public void free() {
            int i;

            mesg = null;
            for (i = 0; i < ids.size(); i++) {
                ids.get(i).clear();
                ids.set(i, null);
            }
            ids.clear();
            ids = null;
        }

        /**
         * Add another id that has generated this message
         *
         * @param id the id
         * @param subId the sub identifier
         */
        public void addId(Type type, String id, String subId) {
            int i, j;
            IdRef ir;

            i = type.ordinal();
            for (j = 0; j < ids.get(i).size(); j++) {
                ir = ids.get(i).get(j);
                if (ir.id.equals(id)) {
                    ir.addSubId(subId);
                    break;
                }
            }
            if (j == ids.get(i).size()) {
                ids.get(i).add(new IdRef(id, subId));
            }
        }

        /**
         * Produce a report on this message...
         *
         * @param type type of messages being reported on
         * @param w the writer on which to write
         * @throws IOException
         */
        public void report(Type type, Writer w) throws IOException {
            ArrayList<IdRef> id;
            int i, j;

            i = type.ordinal();
            id = ids.get(i);
            if (id.size() > 0) {
                w.write("  ");
                w.write(mesg);
                w.write(" {\r\n");
                for (j = 0; j < id.size(); j++) {
                    w.write("    ");
                    w.write(id.get(j).toString());
                    if (i < ids.size() - 1) {
                        w.write(",\r\n");
                    }
                }
                w.write("}\r\n");
            }
        }

        @Override
        public String toString() {
            int i, j;

            StringBuilder sb = new StringBuilder();
            sb.append("'");
            sb.append(mesg);
            sb.append("' index=");
            sb.append(index);
            for (i = 0; i < Type.length; i++) {
                if (ids.get(i) != null) {
                    sb.append(" ");
                    sb.append(Type.toString(i));
                    sb.append(" count=");
                    sb.append(ids.get(i).size());
                    sb.append(" {");
                    for (j = 0; j < ids.get(i).size(); j++) {
                        sb.append("'");
                        sb.append(ids.get(i).get(j).toString());
                        sb.append("'");
                        if (j < ids.get(i).size() - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append("}");
                }
            }
            return sb.toString();
        }
    }

    /**
     * Class that keeps track of an instances of a message. An instance is
     * identified by two identifiers: an id that identifies an instance (e.g. a
     * file); and a subId that identifies a part within the instance.
     */
    final private class IdRef {

        String id;          // object being referenced (e.g. filename)
        ArrayList<String> subIds; // sub identifier (object within instance)
        int count;          // how many times the error occurs in this object

        public IdRef(String id, String subId) {
            this.id = id;
            subIds = new ArrayList<>();
            addSubId(subId);
            count = 1;
        }

        public void addSubId(String subId) {
            if (subId != null && !subId.equals("")) {
                subIds.add(subId);
            }
            count++;
        }

        @Override
        public String toString() {
            int i;
            StringBuilder sb = new StringBuilder();

            sb.append("'");
            sb.append(id);
            sb.append("'");
            if (count > 1) {
                sb.append(" (x");
                sb.append(count);
                sb.append(")");
            }
            if (subIds.size() > 0) {
                sb.append(" in: ");
                for (i = 0; i < subIds.size(); i++) {
                    sb.append("'");
                    sb.append(subIds.get(i));
                    sb.append("'");
                    if (i < subIds.size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            return sb.toString();
        }
    }

    /**
     * Test
     *
     * @param args command line arguments
     */
    public static void main(String args[]) {
        ResultSummary rs;
        StringWriter sw;

        try {
            /*
            // case 1-1, two completely different messages of length 1
            System.out.println("Two completely different messages length 1: a, b");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "a", "id1", null);
            rs.recordResult(Type.ERROR, "b", "id2", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();

            // case 1-2, two identical messages of length 1
            System.out.println("Two completely different messages length 1: a, a");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "a", "id1", null);
            rs.recordResult(Type.ERROR, "a", "id2", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();

            // case 2-1, put in two messages exactly the same
            System.out.println("Two messages the same length: ab, ab");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "ab", "id1", null);
            rs.recordResult(Type.ERROR, "ab", "id2", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();

            // case 2-2, put in two message, the first shorter
            System.out.println("Two messages, the first shorter: ab, abc");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "ab", "id1", null);
            rs.recordResult(Type.ERROR, "abc", "id2", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();

            // case 2-3, put in two message, the second shorter
            System.out.println("Two messages, the second shorter: abc, ab");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "abc", "id1", null);
            rs.recordResult(Type.ERROR, "ab", "id2", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();

            // case 2-4, two completely different messages
            System.out.println("Two completely different messages: a, b");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "a", "id1", null);
            rs.recordResult(Type.ERROR, "b", "id2", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();
             */
            // case 3-1, three identical messages
            System.out.println("Three completely identical messages: 'abc'x3");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "abc", "id1", null);
            rs.recordResult(Type.ERROR, "abc", "id2", null);
            rs.recordResult(Type.ERROR, "abc", "id3", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();
            /*
            // case 3-2, three different tails
            System.out.println("Three different mesgs: 'abcd', 'abce', 'abcf'");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "abcd", "id1", null);
            rs.recordResult(Type.ERROR, "abce", "id2", null);
            rs.recordResult(Type.ERROR, "abcf", "id3", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();

            // case 3-2, four different tails & a longer one
            System.out.println("four different messages: 'abcd', 'abce', 'abcf', 'abceg'");
            rs = new ResultSummary();
            rs.recordResult(Type.ERROR, "abcd", "id1", null);
            rs.recordResult(Type.ERROR, "abceg", "id2", null);
            rs.recordResult(Type.ERROR, "abce", "id3", null);
            rs.recordResult(Type.ERROR, "abcf", "id4", null);
            sw = new StringWriter();
            rs.report(sw);
            System.out.println(sw.toString());
            rs.free();
             */
        } catch (IOException e) {
            System.out.println("Fatal error: " + e.getMessage());
        }
    }
}
