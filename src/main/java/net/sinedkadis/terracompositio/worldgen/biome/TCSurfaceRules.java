package net.sinedkadis.terracompositio.worldgen.biome;

import net.minecraft.world.level.levelgen.SurfaceRules;

public class TCSurfaceRules extends SurfaceRules {
//
//    private static final SurfaceRules.RuleSource PODZOL_BLOCK = makeStateRule(Blocks.PODZOL);
//
//    private static SurfaceRules.RuleSource makeStateRule(Block block) {
//        return SurfaceRules.state(block.defaultBlockState());
//    }
//
//    public static SurfaceRules.RuleSource makeRules() {
//
//
//        return SurfaceRules.sequence(
//                RuleBuilder.of(PODZOL_BLOCK)
////                        .filter(SurfaceRules.isBiome(TCBiomes.FLOW_CEDAR_BIOME))
//                        .filter(SurfaceRules.ON_FLOOR)
//                        .filter(SurfaceRules.noiseCondition(Noises.SURFACE,-0.11515151515151514,1.7976931348623157e+308))
//                        .filter(SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(5), 0))
//                        .build()
//        );
//    }



//    protected static class RuleBuilder {
//        private SurfaceRules.RuleSource rule;
//
//        protected static RuleBuilder of(SurfaceRules.RuleSource source){
//            return new RuleBuilder(source);
//        }
//
//        protected RuleBuilder(SurfaceRules.RuleSource rule) {
//            this.rule = rule;
//        }
//
//        protected RuleBuilder filter(SurfaceRules.ConditionSource condition){
//            rule = SurfaceRules.ifTrue(condition,rule);
//            return this;
//        }
//        protected SurfaceRules.RuleSource build() {
//            return rule;
//        }
//    }
}
