package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import static de.sascp.util.Utility.OHSHIT;

/**
 * Created by Rene on 18.05.2016.
 */
public class shitIsAboutToGoDownMsg extends ChatMessage {
    public shitIsAboutToGoDownMsg() {
        super(null, 0, null, 0, OHSHIT, 0);
    }
}
