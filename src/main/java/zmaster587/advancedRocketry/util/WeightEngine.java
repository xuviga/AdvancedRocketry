package zmaster587.advancedRocketry.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.block.BlockBipropellantRocketMotor;
import zmaster587.advancedRocketry.block.BlockFuelTank;
import zmaster587.advancedRocketry.block.BlockPressurizedFluidTank;
import zmaster587.advancedRocketry.block.BlockRocketMotor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public enum WeightEngine {
    INSTANCE("config/advRocketry/weights.json");

    private final String file;
    private Map<String, Double> weights;

    WeightEngine(String file) {
        this.file = file;
        load();
    }

    public float getWeight(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return 0f;
        }

        String registryName = stack.getItem().getRegistryName().toString();
        double knownWeight = weights.getOrDefault(registryName, -1.0);

        if (knownWeight >= 0) {
            return (float) (knownWeight * stack.getCount());
        }

        // Предустановленные веса
        double tankWeight = 0.2;
        double motorWeight = 2.0;
        double guidanceComputerWeight = 1.8;
        double pressureTankWeight = 5.0;
        double satelliteHatchWeight = 5.0;

        if (stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) stack.getItem()).getBlock();

            if (block instanceof BlockFuelTank) {
                weights.put(registryName, tankWeight);
                return (float) tankWeight;
            }
            if (block instanceof BlockRocketMotor || block instanceof BlockBipropellantRocketMotor) {
                weights.put(registryName, motorWeight);
                return (float) motorWeight;
            }
            if (block instanceof BlockPressurizedFluidTank) {
                weights.put(registryName, pressureTankWeight);
                return (float) pressureTankWeight;
            }
            if ("advancedrocketry:guidancecomputer".equals(registryName)) {
                weights.put(registryName, guidanceComputerWeight);
                return (float) guidanceComputerWeight;
            }
            if ("advancedrocketry:loader".equals(registryName)) {
                weights.put(registryName, satelliteHatchWeight);
                return (float) satelliteHatchWeight;
            }
        }

        // Значение по умолчанию
        weights.put(registryName, 0.1);
        return 0.1f;
    }

    public float getWeight(Collection<ItemStack> stacks) {
        return stacks.stream().map(this::getWeight).reduce(0.0F, Float::sum);
    }

    public float getWeight(World world, BlockPos pos) {
        if (world == null || pos == null) return 0;

        TileEntity te = null;
        Block block = null;

        try {
            te = world.getTileEntity(pos);
        } catch (Exception ignored) {}

        try {
            block = world.getBlockState(pos).getBlock();
        } catch (Exception ignored) {}

        return getWeight(te, block);
    }

    public float getWeight(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return 0;
        return getWeight(stack.getFluid(), stack.amount);
    }

    public float getWeight(Fluid fluid, float amount) {
        if (fluid == null || amount <= 0) return 0;
        double weightPerMb = weights.getOrDefault(fluid.getUnlocalizedName(), -1.0);

        if (weightPerMb >= 0) {
            return (float) (weightPerMb * amount);
        }

        // Значение по умолчанию: 1 кг за 1 литр
        float defaultWeight = 0.001f * amount;
        weights.put(fluid.getUnlocalizedName(), 0.001);
        return defaultWeight;
    }

    public float getTEWeight(TileEntity te) {
        if (!ARConfiguration.getCurrentConfig().advancedWeightSystemInventories || te == null)
            return 0;

        float weight = 0f;

        IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler != null) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                weight += getWeight(itemHandler.getStackInSlot(i));
            }
        }

        IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if (fluidHandler != null) {
            for (IFluidTankProperties tank : fluidHandler.getTankProperties()) {
                if (tank != null && tank.getContents() != null) {
                    weight += getWeight(tank.getContents());
                }
            }
        }

        return weight;
    }

    public float getWeight(TileEntity te, Block block) {
        if (block == null) {
            if (te != null) {
                block = te.getBlockType();
            } else {
                return 0;
            }
        }

        float blockWeight = getWeight(new ItemStack(block));
        return blockWeight + getTEWeight(te);
    }

    public float getWeight(World world, Collection<BlockPos> poses) {
        return poses.stream().map(pos -> getWeight(world, pos)).reduce(0.0F, Float::sum);
    }

    public void load() {
        File f = new File(file);
        if (!f.exists()) {
            weights = new HashMap<>();
            // Предустановленные веса
            weights.put("advancedrocketry:guidancecomputer", 1.8);
            weights.put("advancedrocketry:loader", 5.0);
            weights.put("advancedrocketry:fuelTank", 0.2);
            weights.put("advancedrocketry:rocketMotor", 2.0);
            weights.put("advancedrocketry:rocketMotorBipropellant", 2.0);
            weights.put("advancedrocketry:pressurizedFluidTank", 5.0);
            weights.put("minecraft:chest", 0.5);
            weights.put("minecraft:furnace", 1.5);
            weights.put("minecraft:hopper", 1.0);
            weights.put("minecraft:glass", 0.1);
            weights.put("minecraft:iron_block", 3.0);
            weights.put("minecraft:gold_block", 2.5);
            weights.put("minecraft:diamond_block", 2.0);
            weights.put("minecraft:water_bucket", 1.0);
            weights.put("minecraft:lava_bucket", 2.0);
            weights.put("minecraft:stone", 0.2);
            weights.put("minecraft:cobblestone", 0.2);
            weights.put("minecraft:dirt", 0.1);
            weights.put("minecraft:sand", 0.1);
            weights.put("minecraft:gravel", 0.1);
            save(); // Автосохранение нового файла
            return;
        }
        try (Reader r = new FileReader(f)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            JsonObject root = gson.fromJson(r, JsonObject.class);
            weights = gson.fromJson(root.getAsJsonObject("individual"), HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
            weights = new HashMap<>();
        }
    }


    public void save() {
        try (FileWriter w = new FileWriter(file)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            JsonObject root = new JsonObject();
            root.add("individual", gson.toJsonTree(weights));
            w.write(gson.toJson(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
