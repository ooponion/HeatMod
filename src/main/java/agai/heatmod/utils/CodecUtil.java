package agai.heatmod.utils;

/*
 * Copyright (c) 2024 TeamMoeg
 *
 * This file is part of Frosted Heart.
 *
 * Frosted Heart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Frosted Heart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
 *
 */

import agai.heatmod.utils.builder.ArrayNBTBuilder;
import agai.heatmod.utils.builder.SerializeUtil;
import agai.heatmod.utils.codec.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.DataResult.PartialResult;
import com.mojang.serialization.codecs.EitherMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.MappedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.text2speech.Narrator.LOGGER;

public class CodecUtil {
    public static class DispatchNameCodecBuilder<A>{
        Map<Class<? extends A>,String> classes=new LinkedHashMap<>();
        Map<String,Codec<? extends A>> codecs=new LinkedHashMap<>();
        public <T extends A> DispatchNameCodecBuilder<A> type(String type,Class<T> clazz,Codec<T> codec){
            classes.put(clazz, type);
            codecs.put(type, codec);
            return this;
        }
        public <T extends A> DispatchNameCodecBuilder<A> type(Class<T> clazz,Codec<T> codec){
            String type="n"+classes.size();
            classes.put(clazz, type);
            codecs.put(type, codec);
            return this;
        }
        public Codec<A> buildByName(){
            return Codec.STRING.dispatch(o->ImmutableMap.copyOf(classes).get(o.getClass()), ImmutableMap.copyOf(codecs)::get);
        }
        public Codec<A> buildByInt(){
            List<Class<? extends A>> classes=new ArrayList<>();
            List<Codec<? extends A>> codecs=new ArrayList<>();
            for(Entry<Class<? extends A>, String> name:this.classes.entrySet()) {
                classes.add(name.getKey());
                codecs.add(this.codecs.get(name.getValue()));
            }
            return Codec.INT.dispatch(o->ImmutableList.copyOf(classes).indexOf(o.getClass()), ImmutableList.copyOf(codecs)::get);
        }
        public Codec<A> build(){
            return new CompressDifferCodec<>(buildByName(),buildByInt());
        }
    }
    public static final Codec<long[]> LONG_ARRAY_CODEC=new Codec<long[]>() {

        @Override
        public <T> DataResult<T> encode(long[] input, DynamicOps<T> ops, T prefix) {
            ListBuilder<T> builder=ops.listBuilder();
            for(long inp:input)
                builder.add(ops.createLong(inp));
            return builder.build(prefix);
        }

        @Override
        public <T> DataResult<Pair<long[], T>> decode(DynamicOps<T> ops, T input) {
            return ops.getLongStream(input).map(t->Pair.of(t.toArray(), input));
        }

    };
    public static final Codec<int[]> INT_ARRAY_CODEC=new Codec<int[]>() {

        @Override
        public <T> DataResult<T> encode(int[] input, DynamicOps<T> ops, T prefix) {
            ListBuilder<T> builder=ops.listBuilder();
            for(int inp:input)
                builder.add(ops.createInt(inp));
            return builder.build(prefix);
        }

        @Override
        public <T> DataResult<Pair<int[], T>> decode(DynamicOps<T> ops, T input) {
            return ops.getIntStream(input).map(t->Pair.of(t.toArray(), input));
        }

    };
    public static final Codec<byte[]> BYTE_ARRAY_CODEC=new Codec<byte[]>() {

        @Override
        public <T> DataResult<T> encode(byte[] input, DynamicOps<T> ops, T prefix) {
            ListBuilder<T> builder=ops.listBuilder();
            for(byte inp:input)
                builder.add(ops.createByte(inp));
            return builder.build(prefix);
        }

        @Override
        public <T> DataResult<Pair<byte[], T>> decode(DynamicOps<T> ops, T input) {
            return ops.getByteBuffer(input).map(t->Pair.of(t.array(), input));
        }

    };

    public static final Codec<ItemStack>  ITEMSTACK_STRING_CODEC = new AlternativeCodecBuilder<>(ItemStack.class)
            .add(ItemStack.CODEC)
            .add(ResourceLocation.CODEC.comapFlatMap(t->{
                Item it= RegistryHelper.getItem(t);
                if(it==Items.AIR||it==null)return DataResult.error(()->"Not a item");
                return DataResult.success(new ItemStack(it,1));

            }, t-> RegistryHelper.getRegistryName(t.getItem()))).build();
    public static final Codec<Component>  COMPONENT_CODEC = ExtraCodecs.JSON.xmap(Component.Serializer::fromJson, Component.Serializer::toJsonTree);
    public static final Codec<Integer> POSITIVE_INT = Codec.intRange(0, Integer.MAX_VALUE);
    public static final Codec<Ingredient> INGREDIENT_CODEC = new PacketOrSchemaCodec<>(ExtraCodecs.JSON,o2->DataResult.success(o2.toJson()),o->{
        if(o.isJsonArray()||o.isJsonObject())
            return DataResult.success(Ingredient.fromJson(o));
        if(o.isJsonPrimitive()) {
            try {
                Item i= RegistryHelper.getItem(new ResourceLocation(o.getAsString()));
                if(i!=null&&i!=Items.AIR)
                    return DataResult.success(Ingredient.of(i));
            }catch(ResourceLocationException rle) {

            }
        }
        return DataResult.error(()->"Not a ingredient");
    },Ingredient::toNetwork,Ingredient::fromNetwork);
    public static final Codec<Pair<Ingredient,Integer>> INGREDIENT_SIZE_CODEC=CodecUtil.<Pair<Ingredient,Integer>>alternative(Pair.class)
            .add(pairCodec("base_ingredient",INGREDIENT_CODEC,"count", Codec.INT))
            .add(INGREDIENT_CODEC.xmap(o->Pair.of(o, 1), o->o.getFirst()))
            .build();
    public static final Codec<MobEffectInstance> MOB_EFFECT_CODEC = CompoundTag.CODEC.xmap(o->MobEffectInstance.load(o),t->t.save(new CompoundTag()));
    public static final Codec<boolean[]> BOOLEANS = Codec.BYTE.xmap(SerializeUtil::readBooleans, SerializeUtil::writeBooleans);
    /**
     * use {@link Codec#optionalFieldOf(String, Object) optinalFieldOf} in Codec instead for better support
     *
     * */
    @Deprecated
    public static <A> DefaultValueCodec<A> defaultValue(Codec<A> val, A def) {
        return defaultSupply(val, () -> def);
    }
    public static <A> MapCodec<A> fieldOfs(Codec<A> val, String...keys) {
        return new KeysCodec<>(val,keys);
    }
    public static <A,T> MapCodec<A> optionalFieldOfs(Codec<A> val,Function<DynamicOps<T>,T> def, String...keys) {
        return new KeysCodec<>(val,def,keys);
    }
    public static <A> DefaultValueCodec<A> defaultSupply(Codec<A> val, Supplier<A> def) {
        return new DefaultValueCodec<A>(val, def);
    }
    public static <T,A extends List<T>> Codec<A> list(Codec<T> codec,Supplier<A> createList){
        return new CustomListCodec<>(codec,createList);
    }
    public static <A> Codec<List<A>> discreteList(Codec<A> codec){
        return new DiscreteListCodec<>(codec,t->t==null,()->null,"i");
    }
    public static <A> Codec<Stream<A>> streamCodec(Codec<A> codec) {
        return new StreamCodec<>(codec);
    }
    public static <K, V> Codec<Pair<K, V>> pairCodec(String nkey, Codec<K> key, String nval, Codec<V> val) {
        return RecordCodecBuilder.create(t -> t.group(key.fieldOf(nkey).forGetter(Pair::getFirst), val.fieldOf(nval).forGetter(Pair::getSecond))
                .apply(t, Pair::of));
    }
    /**
     * store map in format like:
     * {
     *   "key":{value}
     * }
     * Note that keyCodec must output string type, otherwise an exception would be thrown
     * */
    public static <K, V> Codec<Map<K, V>> mapCodec(Codec<K> keyCodec, Codec<V> valueCodec) {
        return Codec.compoundList(keyCodec, valueCodec).xmap(pl -> pl.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)),
                pl -> pl.entrySet().stream().map(ent -> Pair.of(ent.getKey(), ent.getValue())).collect(Collectors.toList()));
    }
    /**
     * store map in format like:
     * [
     *   {
     *     "nkey":{key},
     *     "nvalue":{value}
     *   }
     * ]
     * key and value could be anything
     * */
    public static <K, V> Codec<Map<K, V>> mapCodec(String nkey, Codec<K> keyCodec, String nval, Codec<V> valueCodec) {
        return Codec.list(CodecUtil.pairCodec(nkey, keyCodec, nval, valueCodec)).xmap(pl -> pl.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)),
                pl -> pl.entrySet().stream().map(ent -> Pair.of(ent.getKey(), ent.getValue())).collect(Collectors.toList()));
    }
    /*
    public static <A,B> Codec<Map<A,B>> toMap(Codec<List<Pair<A,B>>> codec){
        return codec.xmap(l->l.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond,(k1,k2)->k2,LinkedHashMap::new)), l->l.entrySet().stream().map(t->Pair.of(t.getKey(), t.getValue())).collect(Collectors.toList()));
    }*/
    public static <A> Codec<A> createIntCodec(MappedRegistry<A> registry) {
        return Codec.INT.xmap(registry::byId, registry::getId);
    }
    public static <S> AlternativeCodecBuilder<S> alternative(Class<? super S> type){
        return new AlternativeCodecBuilder<>(type);
    }

    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> en){

        return enumCodec(en.getEnumConstants());
    }
    public static <T extends Enum<T>> Codec<T> enumCodec(T[] values){
        Map<String,T> maps=new HashMap<>();
        Map<T,String> maps2=new HashMap<>();
        for(T val:values) {
            maps.put(val.name().toLowerCase(), val);
            maps2.put(val, val.name().toLowerCase());
        }
        return new CompressDifferCodec<>(Codec.STRING.xmap(maps::get, maps2::get),Codec.BYTE.xmap(o->values[o], v->(byte)v.ordinal()));
    }
    public static <O> BooleansCodec.BooleanCodecBuilder<O> booleans(String flag){
        return new BooleansCodec.BooleanCodecBuilder<O>(flag);
    }
    public static <S,A,B> RecordCodecBuilder<S, Either<A, B>> either(
            MapCodec<A> a,MapCodec<B> b,
            Function<S,A> fa,Function<S,B> fb){
        return new EitherMapCodec<>(a,b).forGetter(o->{
            A va=fa.apply(o);
            if(va!=null)
                return Either.left(va);
            return Either.right(fb.apply(o));
        });
    }
    public static <S,A> RecordCodecBuilder<S, A> poly(
            MapCodec<A> readWrite,MapCodec<A> readOnly,
            Function<S,A> getter){
        return new EitherMapCodec<>(readWrite,readOnly).xmap(t->t.left().or(()->t.right()).get(), t->Either.left(t)).forGetter(getter);
    }
    public static <A> Codec<A> debugCodec(Codec<A> codec){
        return new Codec<>() {

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                LOGGER.debug("Encoding Codec: " + codec);
                LOGGER.debug("Encoding Data: " + input);
                DataResult<T> res=codec.encode(input, ops, prefix);
                LOGGER.debug("Encoded result: " + res);
                return res;
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                LOGGER.debug("Decoding Codec: " + codec);
                LOGGER.debug("Decoding Data: " + input);
                DataResult<Pair<A, T>> res=codec.decode(ops,input);
                LOGGER.debug("Decoded result: " + res);
                return res;
            }
            @Override
            public String toString() {
                return codec.toString();
            }
        };
    }
    public static <A> Codec<A> catchingCodec(Codec<A> codec){
        return new Codec<>() {

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                try {
                    DataResult<T> dr= codec.encode(input, ops, prefix);
                    Optional<PartialResult<T>> error=dr.error();
                    if(error.isPresent())
                        LOGGER.warn("encoding "+input+" got error "+error.get().message());
                    return dr;
                }catch(Exception ex) {
                    ex.printStackTrace();
                    LOGGER.warn("Exception has thrown when encoding "+input);
                    return DataResult.error(()->"Exception "+ex.getMessage()+" has thrown when encoding "+input);
                }

            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                try {
                    DataResult<Pair<A, T>> dr= codec.decode(ops,input);
                    Optional<PartialResult<Pair<A, T>>> error=dr.error();
                    if(error.isPresent())
                        LOGGER.warn("decoding "+input+" got error "+error.get().message());
                    return dr;
                }catch(Exception ex) {
                    ex.printStackTrace();
                    LOGGER.warn("Exception has thrown when decoding "+input);
                    return DataResult.error(()->"Exception "+ex.getMessage()+" has thrown when decoding "+input);
                }
            }
            @Override
            public String toString() {
                return codec.toString();
            }
        };
    }
    public static <A> MapCodec<A> debugCodec(MapCodec<A> codec){
        return new MapCodec<>() {

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                LOGGER.debug("Decoding Codec: " + codec);
                LOGGER.debug("Decoding Data: " + input);
                DataResult<A> res=codec.decode(ops, input);
                LOGGER.debug("Decoded result: " + res);
                return res;
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                LOGGER.debug("Encoding Codec: " + codec);
                LOGGER.debug("Encoding Data: " + input);
                RecordBuilder<T> res=codec.encode(input, ops, prefix);

                LOGGER.debug("Encoded result: " + res.build(ops.empty()));
                //encode twice since the record builder clears itself after build
                res=codec.encode(input, ops, prefix);
                return res;
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return codec.keys(ops);
            }
            @Override
            public String toString() {
                return codec.toString();
            }
        };
    }
    public static <O,A,B,C> RecordCodecBuilder<O, Either<A, Either<B, C>>> either(
            MapCodec<A> a,MapCodec<B> b,MapCodec<C> c,
            Function<O,A> fa,Function<O,B> fb,Function<O,C> fc){
        return either(a,either(b,c))
                .forGetter(leftRight(fa,leftRight(fb,fc)));
    }
    public static <O,A,B,C,D> RecordCodecBuilder<O, Either<A, Either<B, Either<C, D>>>> either(
            MapCodec<A> a,MapCodec<B> b,MapCodec<C> c,MapCodec<D> d,
            Function<O,A> fa,Function<O,B> fb,Function<O,C> fc,Function<O,D> fd){
        return either(a,either(b,either(c,d)))
                .forGetter(leftRight(fa,leftRight(fb,leftRight(fc,fd))));
    }
    public static <O,A,B,C,D,E> RecordCodecBuilder<O, Either<A, Either<B, Either<C, Either<D, E>>>>> either(
            MapCodec<A> a,MapCodec<B> b,MapCodec<C> c,MapCodec<D> d,MapCodec<E> e,
            Function<O,A> fa,Function<O,B> fb,Function<O,C> fc,Function<O,D> fd,Function<O,E> fe){
        return either(a,either(b,either(c,either(d,e))))
                .forGetter(leftRight(fa,leftRight(fb,leftRight(fc,leftRight(fd,fe)))));
    }
    public static <O, A, B, C, D, E, F> RecordCodecBuilder<O, Either<A, Either<B, Either<C, Either<D, Either<E, F>>>>>> either(
            MapCodec<A> a, MapCodec<B> b, MapCodec<C> c, MapCodec<D> d, MapCodec<E> e, MapCodec<F> f,
            Function<O, A> fa, Function<O, B> fb, Function<O, C> fc, Function<O, D> fd, Function<O, E> fe, Function<O, F> ff) {
        return either(a, either(b, either(c, either(d, either(e, f)))))
                .forGetter(leftRight(fa, leftRight(fb, leftRight(fc, leftRight(fd, leftRight(fe, ff))))));
    }

    public static <O, A, B, C, D, E, F, G> RecordCodecBuilder<O, Either<A, Either<B, Either<C, Either<D, Either<E, Either<F, G>>>>>>> either(
            MapCodec<A> a, MapCodec<B> b, MapCodec<C> c, MapCodec<D> d, MapCodec<E> e, MapCodec<F> f, MapCodec<G> g,
            Function<O, A> fa, Function<O, B> fb, Function<O, C> fc, Function<O, D> fd, Function<O, E> fe, Function<O, F> ff, Function<O, G> fg) {
        return either(a, either(b, either(c, either(d, either(e, either(f, g))))))
                .forGetter(leftRight(fa, leftRight(fb, leftRight(fc, leftRight(fd, leftRight(fe, leftRight(ff, fg)))))));
    }

    public static <O, A, B, C, D, E, F, G, H> RecordCodecBuilder<O, Either<A, Either<B, Either<C, Either<D, Either<E, Either<F, Either<G, H>>>>>>>> either(
            MapCodec<A> a, MapCodec<B> b, MapCodec<C> c, MapCodec<D> d, MapCodec<E> e, MapCodec<F> f, MapCodec<G> g, MapCodec<H> h,
            Function<O, A> fa, Function<O, B> fb, Function<O, C> fc, Function<O, D> fd, Function<O, E> fe, Function<O, F> ff, Function<O, G> fg, Function<O, H> fh) {
        return either(a, either(b, either(c, either(d, either(e, either(f, either(g, h)))))))
                .forGetter(leftRight(fa, leftRight(fb, leftRight(fc, leftRight(fd, leftRight(fe, leftRight(ff, leftRight(fg, fh))))))));
    }
    public static <O,A,B> Function<O,Either<A,B>> leftRight(Function<O,A> a,Function<O,B> b){
        return o->{
            A va=a.apply(o);
            return va!=null?Either.left(va):Either.right(b.apply(o));
        };
    }
    public static <A,B> MapCodec<Either<A,B>> either(MapCodec<A> a,MapCodec<B> b){
        return new EitherMapCodec<>(a,b);
    }
    public static <T> Codec<T[]> array(Codec<T> codec, T[] arr) {
        return Codec.list(codec).xmap(l -> l.toArray(arr), Arrays::asList);
    }
    public static <T> Codec<T> array(Codec<Object> codec, IntFunction<T> arr) {
        return Codec.list(codec).xmap(l -> {
            Object[] obj = l.toArray();
            T ar = arr.apply(obj.length);
            for (int i = 0; i < obj.length; i++)
                Array.set(ar, i, obj[i]);
            return ar;
        }, Arrays::asList);
    }
    public static <T> MapCodec<T> path(Codec<T> codec,String... path){
        String path0=path[0];
        if(path.length>1) {
            return new MapPathCodec<>(codec,Arrays.copyOfRange(path, 1, path.length)).fieldOf(path0);
        }
        return codec.fieldOf(path0);
    }
    public static <T> void writeCodec(FriendlyByteBuf pb, Codec<T> codec, T obj) {
        DataResult<Object> ob = codec.encodeStart(DataOps.COMPRESSED, obj);
        Optional<Object> ret = ob.resultOrPartial(t->{throw new EncoderException(t);});
//		System.out.println(ret.get());
        ObjectWriter.writeObject(pb, ret.get());
    }
    public static <T> T readCodec(FriendlyByteBuf pb, Codec<T> codec) {

        Object readed = ObjectWriter.readObject(pb);
//		System.out.println(readed);
        DataResult<T> ob = codec.parse(DataOps.COMPRESSED, readed);
//		System.out.println(ob);
        Optional<T> ret = ob.resultOrPartial(LOGGER::info);
        return ret.get();
    }
    public static <T> void writeCodecNBT(FriendlyByteBuf pb, Codec<T> codec, T obj) {
        DataResult<Tag> ob = codec.encodeStart(NbtOps.INSTANCE, obj);
        Optional<Tag> ret = ob.resultOrPartial(EncoderException::new);
        pb.writeNbt((CompoundTag) ret.get());
    }
    public static <T> T readCodecNBT(FriendlyByteBuf pb, Codec<T> codec) {
        Tag readed = pb.readNbt();
        DataResult<T> ob = codec.parse(NbtOps.INSTANCE, readed);
        Optional<T> ret = ob.resultOrPartial(DecoderException::new);
        return ret.get();
    }
    public static <T> T encodeOrThrow(DataResult<T> result) {
        return result.getOrThrow(true, s->{});
    }
    public static <T, A> T decodeOrThrow(DataResult<Pair<T, A>> result) {
        return result.getOrThrow(true, s->{}).getFirst();
    }
    public static <T, A> T strictDecodeOrThrow(DataResult<Pair<T, A>> result) {
        return result.getOrThrow(false, s->{}).getFirst();
    }
    public static <A> A initEmpty(Codec<A> codec) {
        if(codec instanceof CodecWithFactory<A>)
            return ((CodecWithFactory<A>) codec).getInstance();
        return decodeOrThrow(codec.decode(NbtOps.INSTANCE,new CompoundTag()));
    }
    public static <T> void encodeNBT(Codec<T> codec,CompoundTag nbt,String key,T value) {
        codec.encodeStart(NbtOps.INSTANCE, value).resultOrPartial(LOGGER::debug).ifPresent(t->nbt.put(key,t));
    }
    public static <T> void encodeNBT(MapCodec<T> codec,CompoundTag nbt,T value) {
        codec.encode(value, NbtOps.INSTANCE, codec.compressedBuilder(NbtOps.INSTANCE)).build(nbt);
    }
    public static <T> T decodeNBT(MapCodec<T> codec,CompoundTag nbt) {
        return codec.compressedDecode(NbtOps.INSTANCE, nbt).resultOrPartial(LOGGER::debug).orElse(null);
    }
    public static <T> T decodeNBTIfPresent(Codec<T> codec,CompoundTag nbt,String key) {
        if(nbt.contains(key))
            return codec.parse(NbtOps.INSTANCE, nbt.get(key)).resultOrPartial(LOGGER::debug).orElse(null);
        return null;
    }
    public static <T> T decodeNBT(Codec<T> codec,CompoundTag nbt,String key) {
        if(nbt.contains(key))
            return codec.parse(NbtOps.INSTANCE, nbt.get(key)).resultOrPartial(LOGGER::debug).orElse(null);
        return codec.parse(NbtOps.INSTANCE,NbtOps.INSTANCE.empty()).resultOrPartial(LOGGER::debug).orElse(null);
    }
    public static <T> ListTag toNBTList(Collection<T> stacks, Codec<T> codec) {
        ArrayNBTBuilder<Void> arrayBuilder = ArrayNBTBuilder.create();
        stacks.stream().forEach(t -> arrayBuilder.add(encodeOrThrow(codec.encodeStart(NbtOps.INSTANCE, t))));
        return arrayBuilder.build();
    }
    public static <T> List<T> fromNBTList(ListTag list, Codec<T> codec) {
        List<T> al = new ArrayList<>();
        for (Tag nbt : list) {
            al.add(decodeOrThrow(codec.decode(NbtOps.INSTANCE, nbt)));
        }
        return al;
    }
    public static <A> DispatchNameCodecBuilder<A> dispatch(){
        return new DispatchNameCodecBuilder<A>();
    }
    public static <A> DispatchNameCodecBuilder<A> dispatch(Class<A> clazz){
        return new DispatchNameCodecBuilder<A>();
    }
    public static class Test{
        public int test;

        public Test(int test) {
            super();
            this.test = test;
        }

        public int getTest() {
            return test;
        }

        @Override
        public String toString() {
            return "Test [test=" + test + "]";
        }

    }
    public static void main(String[] args) throws Exception {
//		System.out.println(GeneratorData.CODEC.encodeStart(NbtOps.INSTANCE, new GeneratorData((SpecialDataHolder)null)));
    }
}
