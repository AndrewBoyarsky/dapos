package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.Attachment;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractAttachmentTest {
    @Test
    void testSerializationDeserialization() {
        List<Attachment> attachments = toTest();
        for (Attachment attachment : attachments) {
            doSerializationTest(attachment);
        }
    }

    protected void doSerializationTest(Attachment attachment) {
        ByteBuffer buffer = ByteBuffer.allocate(attachment.size());
        attachment.putBytes(buffer);
        buffer.flip();
        Attachment deserialized = null;
        try {
            Constructor<? extends Attachment> constructor = attachment.getClass().getDeclaredConstructor(ByteBuffer.class);
            deserialized = constructor.newInstance(buffer);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            fail(attachment.getClass() + " should have public constructor with ByteBuffer param which deserialize from bytes that attachment", e);
        }
        assertEquals(attachment, deserialized);
        assertEquals(attachment.size(), buffer.position());
    }


    protected abstract List<Attachment> toTest();
}
