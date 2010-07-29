/*
 * Copyright (c) 2010, EPFL - ARNI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the EPFL nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package ch.epfl.arni.ncutils.examples;

import ch.epfl.arni.ncutils.CodedPacket;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.PacketDecoder;
import ch.epfl.arni.ncutils.UncodedPacket;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

/**
 *
 * This example shows how to use ncutils to build simulate
 * random network coding network. Uncoded packets are first
 * created then they are transformed to the corresponding coded
 * packets. These are linearly combined (as a network would) in
 * new coded packets. The resulting packets are finally decoded
 * with the packet decoder.
 *
 * @author lokeller
 */
public class BlockLevelExample {

    public static void main(String [] args) {

        FiniteField ff = FiniteField.getDefaultFiniteField();

        int blockNumber = 10;
        int payloadLen = 10;
        int payloadLenCoeffs = 20;

        /* create the uncoded packets */
        UncodedPacket[] inputPackets = new UncodedPacket[blockNumber];
        for ( int i = 0 ; i < blockNumber ; i++) {
            byte[] payload = new byte[payloadLen];
            Arrays.fill(payload, (byte) (0XA0 +  i));
            inputPackets[i] = new UncodedPacket(i, payload);
        }

        System.out.println(" Input blocks: ");
        printUncodedPackets(Arrays.asList(inputPackets), payloadLen);

        /* prepare the input packets to be sent on the network */
        CodedPacket[] codewords = new CodedPacket[blockNumber];

        for ( int i = 0 ; i < blockNumber ; i++) {
            codewords[i] = new CodedPacket( inputPackets[i], blockNumber, ff);
        }

        System.out.println(" Codewords: ");
        printCodedPackets(Arrays.asList(codewords), payloadLenCoeffs);

        /* create a set of linear combinations that simulate
         * the output of the network
         */

        CodedPacket[] networkOutput = new CodedPacket[blockNumber];

        Random r = new Random(2131231);

        for ( int i = 0 ; i < blockNumber ; i++) {

            networkOutput[i] = new CodedPacket(blockNumber, payloadLen, ff);

            for ( int j = 0 ; j < blockNumber ; j++) {
                int x = r.nextInt(ff.getCardinality());                
                CodedPacket copy = codewords[j].scalarMultiply(x);
                networkOutput[i] = networkOutput[i].add(copy);
                
            }
        }

        System.out.println(" Network output: ");
        printCodedPackets(Arrays.asList(networkOutput), payloadLenCoeffs);

        /* decode the received packets */
        PacketDecoder decoder = new PacketDecoder(ff, blockNumber, payloadLenCoeffs);

        System.out.println(" Decoded packets: ");
        for ( int i = 0; i < blockNumber ; i++) {
            Vector<UncodedPacket> packets = decoder.addPacket(networkOutput[i]);
            printUncodedPackets(packets, payloadLen);
        }

    }

    private static void printUncodedPackets(Iterable<UncodedPacket> packets, int payloadLen) {
        for (UncodedPacket p : packets) {            
            System.out.println(p);
        }
    }

    private static void printCodedPackets(Iterable<CodedPacket> packets, int payloadLen) {
        for (CodedPacket p : packets) {
            System.out.println(p);
        }
    }

}
