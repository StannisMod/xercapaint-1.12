package xerca.xercapaint.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xerca.xercapaint.common.SoundEvents;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class BrushSound extends MovingSound {
    private int age = 0;
    private int fadingTicks = 4;
    private Random random;

    private static final float[] fadeVolumes = {0.0f, 0.3f, 0.7f};

    public BrushSound() {
        super(SoundEvents.STROKE_LOOP, SoundCategory.MASTER);
        volume = 1.0f;
        pitch = 1.0F;
        repeat = true;
        attenuationType = ISound.AttenuationType.NONE;
        if (Minecraft.getMinecraft().world != null) {
            random = Minecraft.getMinecraft().world.rand;
        }
    }

    public void stopSound() {
        age += 300;
    }

    public void refreshFade() {
        fadingTicks = 3;
        volume = 1.0f;
    }

    @Override
    public void update() {
        age++;
        if (fadingTicks <= 0 && age > 300) {
            this.donePlaying = true;
        }
        if (fadingTicks >= 0) {
            if (fadingTicks < fadeVolumes.length) {
                volume = fadeVolumes[fadingTicks];
            }
            fadingTicks--;
        }

        float randomPitchChange = 0.03f - random.nextFloat() * 0.06f;
        if (pitch >= 1.2f && randomPitchChange > 0.f) {
            randomPitchChange -= 0.03f;
        } else if (pitch <= 0.8f && randomPitchChange < 0.f) {
            randomPitchChange += 0.03f;
        }
        pitch += randomPitchChange;
    }
}
