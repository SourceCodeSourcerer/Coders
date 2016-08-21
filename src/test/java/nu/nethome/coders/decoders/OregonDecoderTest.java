package nu.nethome.coders.decoders;

import nu.nethome.coders.decoders.util.JirFileTestPlayer;
import nu.nethome.util.ps.FieldValue;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class OregonDecoderTest {

    OregonDecoder decoder;
    ProtocolDecoderSink sink;
    ArgumentCaptor<ProtocolMessage> messageCaptor;

    @Before
    public void setUp() throws Exception {
        decoder = new OregonDecoder();
        sink = mock(ProtocolDecoderSink.class);
        messageCaptor = ArgumentCaptor.forClass(ProtocolMessage.class);
        decoder.setTarget(sink);
    }

    @Test
    public void tempHumiditySensor1D20WithKnownTestVector() throws Exception {
        receiveMessage("A1D2016B1091073A14");
        verifyTemperature(0x1D20, 190);
        assertThat(getMessageField("Moisture"), is(37));
    }

    @Test
    public void tempHumiditySensor1D20WithKnownTestVectorDirectly() throws Exception {
        final byte message[] = { 0xA, 0x1, 0xD, 0x2, 0x0, 0x1, 0x6, 0xB, 0x1, 0x0, 0x9, 0x1, 0x0, 0x7, 0x3, 0xA, 0x1, 0x4 };
        decoder.decodeMessage(message);
        verifyTemperature(0x1D20, 190);
        assertThat(getMessageField("Moisture"), is(37));
    }

    @Test
    public void tempHumiditySensor1D20WithLowBattery() throws Exception {
        receiveMessage("A1D2016B5091073A54");
        verify(sink, times(1)).parsedMessage(messageCaptor.capture());
        assertThat(getMessageField("LowBattery"), is(1));
    }

    // Ref:
    // http://wmrx00.sourceforge.net/Arduino/OregonScientific-RF-Protocols.pdf
    @Test
    public void tempHumiditySensor1D20WithLowBatteryAndChannelShouldReallyBe3() throws Exception {
        receiveMessage("A1D20485C480882835");
        verify(sink, times(1)).parsedMessage(messageCaptor.capture());
        assertThat(getMessageField("LowBattery"), is(1));
        assertThat(getMessageField("SensorId"), is(0x1D20));
        assertThat(getMessageField("Channel"), is(3));
        assertThat(getMessageField("Id"), is(0x85));
        assertThat(getMessageField("Temp"), is(-84));
        assertThat(getMessageField("Moisture"), is(28));
    }

    // Ref:
    // http://wmrx00.sourceforge.net/Arduino/OregonScientific-RF-Protocols.pdf
    @Test
    public void tempHumiditySensor1D20WithLowBatteryAndChannelShouldReallyBe1() throws Exception {
        receiveMessage("A1D2016B1091073A14");
        verify(sink, times(1)).parsedMessage(messageCaptor.capture());
        assertThat(getMessageField("LowBattery"), is(0));
        assertThat(getMessageField("SensorId"), is(0x1D20));
        assertThat(getMessageField("Channel"), is(1));
        assertThat(getMessageField("Id"), is(0x6B));
        assertThat(getMessageField("Temp"), is(190));
        assertThat(getMessageField("Moisture"), is(37));
    }

    @Test
    public void tempHumiditySensorF824WithKnownTestVector() throws Exception {
        receiveMessage("AF82416B1091073AE4");
        verifyTemperature(0xF824, 190);
        assertThat(getMessageField("Moisture"), is(37));
    }

    @Test
    public void tempHumiditySensorF8B4WithKnownTestVector() throws Exception {
        receiveMessage("AF8B416B1091073A75");
        verifyTemperature(0xF8B4, 190);
        assertThat(getMessageField("Moisture"), is(37));
    }

    @Test
    public void tempSensorEC40WithKnownTestVector() throws Exception {
        receiveMessage("AEC4016B1091834");
        verifyTemperature(0xEC40, -190);
    }

    @Test
    public void tempSensorC844WithKnownTestVector() throws Exception {
        receiveMessage("AC84416B1091814");
        verifyTemperature(0xC844, -190);
    }

    @Test
    public void windSensor1984WithKnownTestVector() throws Exception {
        receiveMessage("A198416B1800063892D4");
        verifyWind(0x1984);
    }

    @Test
    public void windSensor1994WithKnownTestVector() throws Exception {
        receiveMessage("A199416B1800063892E4");
        verifyWind(0x1994);
    }

    @Test
    public void rainSensor2D10WithKnownTestVector() throws Exception {
        receiveMessage("A2D1016B15211235063");
        verifyRain(0x2D10);
    }

    @Test
    public void rainSensor2914WithKnownTestVector() throws Exception {
        receiveMessage("A29140091498896771055");
        verify(sink, times(1)).parsedMessage(messageCaptor.capture());
        assertThat(getMessageField("SensorId"), is(0x2914));
        assertThat(getMessageField("Channel"), is(0));
        assertThat(getMessageField("Id"), is(0x9));
        assertThat(getMessageField("LowBattery"), is(0));
        assertThat(getMessageField("RainRate"), is(8894));
        assertThat(getMessageField("TotalRain"), is(17769));
    }

    @Test
    public void tempHumidityPressureSensor5D60WithKnownTestVector() throws Exception {
        receiveMessage("A5D6016B109107300FF1E5");
        verifyTemperature(0x5D60, 190);
        assertThat(getMessageField("Moisture"), is(37));
        assertThat(getMessageField("Pressure"), is(511));
    }

    private void verifyTemperature(int sensorId, int temperature) {
        verify(sink, times(1)).parsedMessage(messageCaptor.capture());
        assertThat(getMessageField("SensorId"), is(sensorId));
        assertThat(getMessageField("Channel"), is(1));
        assertThat(getMessageField("Id"), is(0x6B));
        assertThat(getMessageField("Temp"), is(temperature));
        assertThat(getMessageField("LowBattery"), is(0));
    }

    private void verifyWind(int sensorId) {
        verify(sink, times(1)).parsedMessage(messageCaptor.capture());
        assertThat(getMessageField("SensorId"), is(sensorId));
        assertThat(getMessageField("Channel"), is(1));
        assertThat(getMessageField("Id"), is(0x6B));
        assertThat(getMessageField("LowBattery"), is(0));
        assertThat(getMessageField("Direction"), is(8));
        assertThat(getMessageField("Wind"), is(360));
        assertThat(getMessageField("AverageWind"), is(298));
    }

    private void verifyRain(int sensorId) {
        verify(sink, times(1)).parsedMessage(messageCaptor.capture());
        assertThat(getMessageField("SensorId"), is(sensorId));
        assertThat(getMessageField("Channel"), is(1));
        assertThat(getMessageField("Id"), is(0x6B));
        assertThat(getMessageField("LowBattery"), is(0));
        assertThat(getMessageField("RainRate"), is(125));
        assertThat(getMessageField("TotalRain"), is(5321));
    }

    private void receiveMessage(String s) {
        for (char c : s.toCharArray()) {
            decoder.addNibble(Byte.parseByte("" + c, 16));
        }
    }

    public int getMessageField(String fieldName) {
        List<FieldValue> fields = messageCaptor.getValue().getFields();
        for (FieldValue field : fields) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return -1;
    }

    @Test
    public void basicJir() {
        JirFileTestPlayer player = new JirFileTestPlayer(JirFileTestPlayer.OREGON_DECODER);

        // Using a known test vector
        player.playFile(this.getClass().getClassLoader().getResourceAsStream("nu/nethome/coders/decoders/oregon1.jir"));

        assertThat(player.getMessageField(0, "SensorId"), is(0x1D20));
        assertThat(player.getMessageField(0, "Channel"), is(1));
        assertThat(player.getMessageField(0, "Id"), is(0xEB));
        assertThat(player.getMessageField(0, "Temp"), is(263));
        assertThat(player.getMessageField(0, "Moisture"), is(20));
        assertThat(player.getMessageField(0, "LowBattery"), is(0));
    }
}
