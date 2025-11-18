package net.sinedkadis.terracompositio.item.models.amimations;
// Save this class in your mod and generate all required imports
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
/**
 * Made with Blockbench 5.0.3
 * Exported for Minecraft version 1.19 or later with Mojang mappings
 * @author Author
 */
public class TechnetiumArmorAnimations {
	public static final AnimationDefinition BOOTS_IDLE;

	static {
		AnimationChannel.Interpolation interpolation = AnimationChannel.Interpolations.CATMULLROM;
        BOOTS_IDLE = AnimationDefinition.Builder.withLength(10.0F).looping()
			.addAnimation("left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), interpolation),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), interpolation),
				new Keyframe(10.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), interpolation)
			))
			.addAnimation("right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), interpolation),
				new Keyframe(5.0F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), interpolation),
				new Keyframe(10.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), interpolation)
			))
			.build();
	}
}