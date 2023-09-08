package shcm.shsupercm.fabric.citresewn.pack.cits;

import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.ex.CITParseException;
import shcm.shsupercm.fabric.citresewn.mixin.core.NbtCompoundAccessor;
import shcm.shsupercm.fabric.citresewn.pack.CITPack;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class CIT {
    public final CITPack pack;
    public final ResourceLocation propertiesIdentifier;

    public final Set<Item> items = new HashSet<>();

    public final int damageMin, damageMax;
    public final boolean damageAny, damageRange, damagePercentage;
    public final Integer damageMask;

    public final int stackMin, stackMax;
    public final boolean stackAny, stackRange;

    public final Set<ResourceLocation> enchantments = new HashSet<>();
    public final List<Tuple<Integer, Integer>> enchantmentLevels = new ArrayList<>();
    public final boolean enchantmentsAny, enchantmentLevelsAny;

    public final InteractionHand hand;

    public final Predicate<CompoundTag> nbt;

    public final int weight;

    public CIT(CITPack pack, ResourceLocation identifier, Properties properties) throws CITParseException {
        this.pack = pack;
        this.propertiesIdentifier = identifier;
        try {
            for (String itemId : (properties.getProperty("items", properties.getProperty("matchItems", " "))).split(" "))
                if (!itemId.isEmpty()) {
                    ResourceLocation itemIdentifier = new ResourceLocation(itemId);
                    if (!Registry.ITEM.containsKey(itemIdentifier))
                        throw new Exception("Unknown item " + itemId);
                    this.items.add(Registry.ITEM.get(itemIdentifier));
                }
            if (this.items.isEmpty())
                try {
                    String id = propertiesIdentifier.getPath().substring(0, propertiesIdentifier.getPath().length() - 11);
                    String[] split = id.split("/");
                    id = split[split.length - 1];
                    ResourceLocation itemId = new ResourceLocation(propertiesIdentifier.getNamespace(), id);
                    if (Registry.ITEM.containsKey(itemId))
                        this.items.add(Registry.ITEM.get(itemId));
                } catch (Exception ignored) { }

            String damage = properties.getProperty("damage");
            if (damageAny = damage == null) {
                this.damageRange = false;
                this.damagePercentage = false;
                this.damageMin = 0;
                this.damageMax = 0;
            } else {
                if (this.damagePercentage = damage.contains("%"))
                    damage = damage.replace("%", "");

                if (damage.contains("-")) {
                    String[] split = damage.split("-");
                    if (split.length > 2)
                        throw new Exception("damage range must have up to 2 numbers");

                    this.damageMin = split[0].isEmpty() ? Integer.MIN_VALUE : Integer.parseInt(split[0]);
                    this.damageMax = split.length == 1 ? Integer.MAX_VALUE : Integer.parseInt(split[1]);

                    if (this.damageMin > this.damageMax)
                        throw new Exception("damage range min is higher than max");

                    this.damageRange = this.damageMin < this.damageMax;
                } else {
                    this.damageRange = false;
                    this.damageMin = this.damageMax = Integer.parseInt(damage);
                }
            }

            this.damageMask = properties.containsKey("damageMask") ? Integer.parseInt(properties.getProperty("damageMask")) : null;

            String stackSize = properties.getProperty("stackSize");
            if (stackAny = stackSize == null) {
                this.stackRange = false;
                this.stackMin = 0;
                this.stackMax = 0;
            } else {
                if (stackSize.contains("-")) {
                    String[] split = stackSize.split("-");
                    if (split.length > 2)
                        throw new Exception("stackSize range must have up to 2 numbers");

                    this.stackMin = split[0].isEmpty() ? Integer.MIN_VALUE : Integer.parseInt(split[0]);
                    this.stackMax = split.length == 1 ? Integer.MAX_VALUE : Integer.parseInt(split[1]);

                    if (this.stackMin > this.stackMax)
                        throw new Exception("stackSize range min is higher than max");

                    this.stackRange = this.stackMin < this.stackMax;
                } else {
                    this.stackRange = false;
                    this.stackMin = this.stackMax = Integer.parseInt(stackSize);
                }
            }

            String enchantmentIDs = properties.getProperty("enchantments", properties.getProperty("enchantmentIDs"));
            if (!(this.enchantmentsAny = enchantmentIDs == null)) {
                for (String ench : enchantmentIDs.split(" ")) {
                    ResourceLocation enchIdentifier = new ResourceLocation(ench);
                    if (!Registry.ENCHANTMENT.containsKey(enchIdentifier))
                        CITResewn.logWarnLoading("CIT Warning: Unknown enchantment " + enchIdentifier);
                    this.enchantments.add(enchIdentifier);
                }
            }

            String enchantmentLevelsProp = properties.getProperty("enchantmentLevels");
            if (!(this.enchantmentLevelsAny = enchantmentLevelsProp == null)) {
                for (String range : enchantmentLevelsProp.split(" ")) {
                    if (range.contains("-")) {
                        if (range.startsWith("-")) {
                            range = range.substring(1);
                            if (range.contains("-"))
                                throw new Exception("enchantmentLevels ranges must have up to 2 numbers each");
                            this.enchantmentLevels.add(new Tuple<>(0, Integer.parseInt(range)));
                        } else if (range.endsWith("-")) {
                            range = range.substring(0, range.length() - 1);
                            if (range.contains("-"))
                                throw new Exception("enchantmentLevels ranges must have up to 2 numbers each");
                            this.enchantmentLevels.add(new Tuple<>(Integer.parseInt(range), Integer.MAX_VALUE));
                        } else {
                            String[] split = range.split("-");
                            if (split.length != 2)
                                throw new Exception("enchantmentLevels ranges must have up to 2 numbers each");
                            Tuple<Integer, Integer> minMaxPair = new Tuple<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                            if (minMaxPair.getA() > minMaxPair.getB())
                                throw new Exception("enchantmentLevels range min is higher than max");
                            this.enchantmentLevels.add(minMaxPair);
                        }
                    } else {
                        int level = Integer.parseInt(range);
                        this.enchantmentLevels.add(new Tuple<>(level, level));
                    }
                }
            }

            this.hand = switch (properties.getProperty("hand", "any")) {
                case "main" -> Hand.MAIN_HAND;
                case "off" -> Hand.OFF_HAND;
                default -> null;
            };

            List<Predicate<CompoundTag>> nbtPredicates = new ArrayList<>();
            for (Object o : properties.keySet())
                if (o instanceof String property && property.startsWith("nbt.")) {
                    String matchProperty = properties.getProperty(property);
                    final String[] path = property.substring(4).split("\\.");
                    final Predicate<String> match;
                    final boolean caseSensitive = !matchProperty.startsWith("i");

                    if (matchProperty.startsWith(caseSensitive ? "pattern:" : "ipattern:")) {
                        matchProperty = caseSensitive ? matchProperty.substring(8) : matchProperty.substring(9).toLowerCase(Locale.ENGLISH);
                        final String pattern = matchProperty;
                        match = s -> matchesPattern(caseSensitive ? s : s.toLowerCase(), pattern, 0, s.length(), 0, pattern.length());
                    } else if (matchProperty.startsWith(caseSensitive ? "regex:" : "iregex:")) {
                        matchProperty = caseSensitive ? matchProperty.substring(6) : matchProperty.substring(7).toLowerCase(Locale.ENGLISH);
                        final Pattern pattern = Pattern.compile(matchProperty);
                        match = s -> pattern.matcher(caseSensitive ? s : s.toLowerCase()).matches();
                    } else {
                        if (property.equals("nbt.display.color") && matchProperty.startsWith("#"))
                            try {
                                matchProperty = String.valueOf(Integer.parseInt(matchProperty.substring(1).toLowerCase(Locale.ENGLISH), 16));
                            } catch (Exception ignored) { }

                        final String pattern = matchProperty;
                        match = s -> s.equals(pattern);
                    }

                    final boolean checkJson = (path[path.length - 1].equals("Name") || (path.length >= 2 && path[path.length - 2].equals("Lore"))) && !((matchProperty.startsWith("{") || matchProperty.startsWith("\\{")) && matchProperty.endsWith("}"));

                    nbtPredicates.add(new Predicate<CompoundTag>() {
                        public boolean test(Tag nbtElement, int index) {
                            if (index >= path.length) {
                                if (nbtElement instanceof StringTag nbtString) {
                                    String text = nbtString.getAsString();
                                    if (checkJson)
                                        try {
                                            //noinspection ConstantConditions
                                            text = Component.Serializer.fromJson(text).getString();
                                        } catch (Exception ignored) { }

                                    return match.test(text);
                                } else if (nbtElement instanceof NumericTag nbtNumber)
                                    return match.test(String.valueOf(nbtNumber.getAsNumber()));
                            } else {
                                String name = path[index];
                                if (name.equals("*")) {
                                    if (nbtElement instanceof CompoundTag nbtCompound) {
                                        for (Tag subElement : ((NbtCompoundAccessor) nbtCompound).getEntries().values())
                                            if (test(subElement, index + 1))
                                                return true;
                                    } else if (nbtElement instanceof ListTag nbtList) {
                                        for (Tag subElement : nbtList)
                                            if (test(subElement, index + 1))
                                                return true;
                                    }
                                } else {
                                    if (nbtElement instanceof CompoundTag nbtCompound) {
                                        Tag subElement = nbtCompound.get(name);
                                        return subElement != null && test(subElement, index + 1);
                                    } else if (nbtElement instanceof ListTag nbtList) {
                                        try {
                                            Tag subElement = nbtList.get(Integer.parseInt(name));
                                            return subElement != null && test(subElement, index + 1);
                                        } catch (Exception ignored) {
                                            return false;
                                        }
                                    }
                                }
                            }
                            return false;
                        }

                        @Override
                        public boolean test(CompoundTag nbtCompound) {
                            return test(nbtCompound, 0);
                        }
                    });
                }
            this.nbt = nbtCompound -> {
                for (Predicate<CompoundTag> predicate : nbtPredicates)
                    if(!predicate.test(nbtCompound))
                        return false;
                return true;
            };

            this.weight = Integer.parseInt(properties.getProperty("weight", "0"));
        } catch (Exception e) {
            throw new CITParseException(pack.resourcePack, identifier, (e.getClass() == Exception.class ? "" : e.getClass().getSimpleName() + ": ") + e.getMessage());
        }
    }

    public boolean test(ItemStack stack, InteractionHand hand, Level world, LivingEntity entity, boolean ignoreItemType) {
        if (!ignoreItemType && !items.isEmpty() && !items.contains(stack.getItem()))
            return false;

        if (!damageAny && stack.getItem().canBeDepleted()) {
            int damage = stack.getDamageValue();
            if (damageMask != null)
                damage &= damageMask;
            if (damagePercentage)
                damage = Math.round(100f * (float) stack.getDamageValue() / (float) stack.getMaxDamage());
            if (damageRange ? (damage < damageMin || damage > damageMax) : (damage != damageMin))
                return false;
        }

        if (!stackAny) {
            int count = stack.getCount();
            if (stackRange ? (count < stackMin || count > stackMax) : (count != stackMin))
                return false;
        }

        if (this.hand != null && this.hand != hand)
            return false;

        if (!enchantmentsAny) {
            Map<ResourceLocation, Integer> stackEnchantments = new LinkedHashMap<>();
            for (Tag nbtElement : stack.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(stack) : stack.getEnchantmentTags())
                stackEnchantments.put(EnchantmentHelper.getEnchantmentId((CompoundTag) nbtElement), EnchantmentHelper.getEnchantmentLevel((CompoundTag) nbtElement));

            boolean matches = false;
            for (ResourceLocation enchantment : enchantments) {
                Integer level = stackEnchantments.get(enchantment);
                if (level != null)
                    if (enchantmentLevelsAny) {
                        if (level > 0) {
                            matches = true;
                            break;
                        }
                    } else
                        for (Tuple<Integer, Integer> levelRange : enchantmentLevels)
                            if (level >= levelRange.getA() && level <= levelRange.getB()) {
                                matches = true;
                                break;
                            }
            }

            if (!matches)
                return false;
        } else if (!enchantmentLevelsAny) {
            Collection<Integer> levels = new ArrayList<>();
            levels.add(0);
            for (Tag nbtElement : stack.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(stack) : stack.getEnchantmentTags())
                levels.add(EnchantmentHelper.getEnchantmentLevel((CompoundTag) nbtElement));

            boolean matches = false;

            l: for (Integer level : levels) {
                for (Tuple<Integer, Integer> levelRange : enchantmentLevels) {
                    if (level >= levelRange.getA() && level <= levelRange.getB()) {
                        matches = true;
                        break l;
                    }
                }
            }

            if (!matches)
                return false;
        }

        return nbt == null || nbt.test(stack.getTag());
    }

    public void dispose() {
        //stub
    }

    /**
     * Takes a defined path and resolves it to an identifier pointing to the resourcepack's path of the specified extension(returns null if no path can be resolved).<br>
     * If definedPath is null, will try to resolve a relative file with the same name as the propertyIdentifier with the extension, otherwise: <br>
     * definedPath will be formatted to replace "\\" with "/" the extension will be appended if not there already. <br>
     * It will first try using definedPath as an absolute path, if it cant resolve(or definedPath starts with ./), definedPath will be considered relative. <br>
     * Relative paths support going to parent directories using "..".
     */
    public static ResourceLocation resolvePath(ResourceLocation propertyIdentifier, String path, String extension, Predicate<ResourceLocation> packContains) {
        if (path == null) {
            path = propertyIdentifier.getPath().substring(0, propertyIdentifier.getPath().length() - 11);
            if (!path.endsWith(extension))
                path = path + extension;
            ResourceLocation pathIdentifier = new ResourceLocation(propertyIdentifier.getNamespace(), path);
            return packContains.test(pathIdentifier) ? pathIdentifier : null;
        }

        ResourceLocation pathIdentifier = new ResourceLocation(path);

        path = pathIdentifier.getPath().replace('\\', '/');
        if (!path.endsWith(extension))
            path = path + extension;

        if (path.startsWith("./"))
            path = path.substring(2);
        else if (!path.contains("..")) {
            pathIdentifier = new ResourceLocation(pathIdentifier.getNamespace(), path);
            if (packContains.test(pathIdentifier))
                return pathIdentifier;
            else if (path.startsWith("assets/")) {
                path = path.substring(7);
                int sep = path.indexOf('/');
                pathIdentifier = new ResourceLocation(path.substring(0, sep), path.substring(sep + 1));
                if (packContains.test(pathIdentifier))
                    return pathIdentifier;
            }
            pathIdentifier = new ResourceLocation(pathIdentifier.getNamespace(), switch (extension) {
                case ".png" -> "textures/";
                case ".json" -> "models/";

                /* UNREACHABLE FAILSAFE */
                default -> "";
            } + path);
            if (packContains.test(pathIdentifier))
                return pathIdentifier;
        }

        LinkedList<String> pathParts = new LinkedList<>(Arrays.asList(propertyIdentifier.getPath().split("/")));
        pathParts.removeLast();

        if (path.contains("/")) {
            for (String part : path.split("/")) {
                if (part.equals("..")) {
                    if (pathParts.size() == 0)
                        return null;
                    pathParts.removeLast();
                } else
                    pathParts.addLast(part);
            }
        } else
            pathParts.addLast(path);
        path = String.join("/", pathParts);

        pathIdentifier = new ResourceLocation(propertyIdentifier.getNamespace(), path);

        return packContains.test(pathIdentifier) ? pathIdentifier : null;
    }

    /**
     * Author: Paul "prupe" Rupe<br>
     * Taken from MCPatcher under public domain licensing.<br>
     * https://bitbucket.org/prupe/mcpatcher/src/1aa45839b2cd029143809edfa60ec59e5ef75f80/newcode/src/com/prupe/mcpatcher/mal/nbt/NBTRule.java#lines-269:301
     */
    public static boolean matchesPattern(String value, String pattern, int curV, int maxV, int curG, int maxG) {
        for (; curG < maxG; curG++, curV++) {
            char g = pattern.charAt(curG);
            if (g == '*') {
                while (true) {
                    if (matchesPattern(value, pattern, curV, maxV, curG + 1, maxG)) {
                        return true;
                    }
                    if (curV >= maxV) {
                        break;
                    }
                    curV++;
                }
                return false;
            } else if (curV >= maxV) {
                break;
            } else if (g == '?') {
                continue;
            }
            if (g == '\\' && curG + 1 < maxG) {
                curG++;
                g = pattern.charAt(curG);
            }

            if (g != value.charAt(curV))
                return false;
        }
        return curG == maxG && curV == maxV;
    }
}
