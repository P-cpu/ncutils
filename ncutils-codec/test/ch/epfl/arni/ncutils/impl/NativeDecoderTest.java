/*
 * Copyright (c) 2011, EPFL - ARNI
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


package ch.epfl.arni.ncutils.impl;

import ch.epfl.arni.ncutils.AbstractDecoderTest;
import ch.epfl.arni.ncutils.CodecFactory;
import ch.epfl.arni.ncutils.Decoder;
import ch.epfl.arni.ncutils.Encoder;
import ch.epfl.arni.ncutils.impl.NativeDecoder;
import ch.epfl.arni.ncutils.impl.NativeEncoder;
import static org.junit.Assert.*;

public class NativeDecoderTest extends AbstractDecoderTest {

	@Override
	public Encoder createEncoder(byte[] segment, int offset, int length,
			int packetsPerSegment) {
		
		assertTrue(CodecFactory.isNativeLibraryAvailable());
		return new NativeEncoder(segment, offset, length, packetsPerSegment);
	}

	@Override
	public Decoder createDecoder(int segmentLength, int packetsPerSegment) {
		
		assertTrue(CodecFactory.isNativeLibraryAvailable());
		return new NativeDecoder(segmentLength, packetsPerSegment);
	}

}
