/*******************************************************************************
 * Copyright (c) 2012, EPFL - ARNI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the EPFL nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package ch.epfl.arni.ncutils;

import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * This class supports decoding of coded packets. It uses a coding vector
 * decoder to compute how to reconstruct uncoded packets and then linearly
 * combine the received packets payloads to reconstruct the uncoded payloads.
 *
 * @author lokeller
 */
public class PacketDecoder {

    private Vector<CodedPacket> packets = new Vector<CodedPacket>();

    private CodingVectorDecoder codingVectorDecoder;

    private int payloadCoordinatesCount;

    private FiniteField ff;

    /**
     * Constructs a new PacketDecoder.
     *
     * @param field the finite field over which the decoder will operate
     * @param maxPackets the maximum number of coded packets, i.e. the length of
     * the coding vectors
     * @param payloadBytesLength the length in bytes of the payload of the packets
     */
    public PacketDecoder(FiniteField field, int maxPackets, int payloadBytesLength) {
        this.ff = field;
        codingVectorDecoder = new CodingVectorDecoder(maxPackets,ff);
        this.payloadCoordinatesCount = ff.coordinatesCount(payloadBytesLength);
    }

    /**
     *
     * Add a coded packet to the decoding buffer and returns all the new uncoded
     * packets that can be decoded thanks to it.
     *
     * @param p a CodedPacket with payload length and coding coefficient length
     * compatible with the decoder
     * @return a vector of uncoded packets that have been decoded thanks to this
     * coded packet (and what was previously added)
     */
    public Vector<UncodedPacket> addPacket(CodedPacket p) {
        
        try {

            Map<Integer, FiniteFieldVector> decoded = codingVectorDecoder.addVector(p.getCodingVector());
            
            /* add the current packet only if it was linearly independant, this
             will be used to decode future packets*/
            packets.add(p);

            /* decode the new packets that can be decoded */
            Vector<UncodedPacket> output = new Vector<UncodedPacket>();
            
            for ( Map.Entry<Integer, FiniteFieldVector> entry : decoded.entrySet() ) {

                FiniteFieldVector decodedPayload = decodePayload(entry.getValue());

                output.add(new UncodedPacket((int) entry.getKey(), decodedPayload));

            }

            return output;

            
        } catch (LinearDependantException ex) {
            return new Vector<UncodedPacket>();
        }
        
    }

    private FiniteFieldVector decodePayload(FiniteFieldVector encoding) {
        
        /* this vector will store the linear combination of coded payloads that
           correspond to the decoded payload */
        FiniteFieldVector decodedPayload = new FiniteFieldVector(payloadCoordinatesCount, ff);
        int[] coordinates3 = decodedPayload.coordinates;
        
        /* linearly combine the payloads */
        
        int[] coordinates = encoding.coordinates;
        
        for (int codedPacketId = 0; codedPacketId < encoding.getLength(); codedPacketId++) {
          
			int coeff = coordinates[codedPacketId];

            /* skip the packet if the coordinate is zero */
            if (coeff == 0) {
                continue;
            }

            FiniteFieldVector codedPayload = packets.get(codedPacketId).getPayload();
            int[] coordinates2 = codedPayload.coordinates;
            
            /* linearly combine the payload of packet "codedPacketId" */
            for (int c = 0; c < codedPayload.getLength(); c++) {               
				int v2 = coordinates2[c];                
				int v1 = coordinates3[c];
                int val = ff.sum[v1][ff.mul[coeff][v2]];
                coordinates3[c] = val;
            }
        }
        return decodedPayload;
    }

    
    public List<CodedPacket> getCodedPackets() {
    	return packets;
    }

    /**
     * Returns the maximum number of packets that can be combined
     *
     * @return the number of packets that can be combined at most
     */
    public int getMaxPackets() {
        return codingVectorDecoder.getMaxPackets();
    }


    /**
     * 
     * Returns the number of linearly independent packets received up to now
     * 
     * @return a number between 0 and getMaxPackets()
     */
    public int getSubspaceSize() {
    	return codingVectorDecoder.getSubspaceSize();
    }
    


}