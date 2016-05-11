package de.sascp.util;

/**
 * Protocol Parser presents functionality for checking Common Header and Message Type specific Header Information
 * against the Protocol Specification
 */
public class ProtocolParser {
    /**
     * Checks the Common Header of a byte[] Stream against the protocol specification
     * @param incomingData - byte[] Stream which will be checked
     * @return returns true if Common Header matches Protocol Specification
     */
    public static boolean checkCommonHeader(byte[] incomingData){

        // TODO

        return true;
    }

    /**
     * Checks message type specific header against protocol specification
     * @param incomingData - byte[] Stream which will be checked
     * @param messageType - expected Message Type
     * @return returns true if Header matches Protocol Specification of given MessageType
     */
    public static boolean checkMessageHeader(byte[] incomingData, int messageType){

        // TODO

        return true;
    }
}
