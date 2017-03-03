/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.network;

import java.io.Serializable;

/**
 *  Defines the generic object that will be passed in every socket, encapsulating
 *  the actual data clients and server wants to exchange. 
 * @author cheikh
 */
public class SocketPacket implements Serializable {
    
    private static final long serialVersionUID = -2186253845488488331L;
    
    private PacketType type;
    private Object object;
    private String msg;

    public SocketPacket(PacketType type, Object obj) {
        this.type = type;
        object = obj;
        if (obj instanceof String) {
            msg = (String) obj;
        }
        else {
            msg = null;
        }
    }

    public PacketType getType() {
        return type;
    }
        
    public String getMsg() {
        return msg;
    }

    public Object getObject() {
        return object;
    }
    
    
    
    
    
}
