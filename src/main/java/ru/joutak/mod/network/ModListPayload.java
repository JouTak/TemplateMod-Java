package ru.joutak.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ru.joutak.mod.ModData;

import java.util.List;

public record ModListPayload(List<ModData> mods) implements CustomPacketPayload {

    public static final Type<ModListPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mod_checker", "main"));

    public static final StreamCodec<FriendlyByteBuf, ModListPayload> CODEC = CustomPacketPayload.codec(
            ModListPayload::write, ModListPayload::new
    );

    public ModListPayload(FriendlyByteBuf buf) {
        this(buf.readList(b -> new ModData(b.readUtf(256), b.readUtf(256))));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.mods, (b, m) -> {
            b.writeUtf(m.id,256);
            b.writeUtf(m.version,256);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}