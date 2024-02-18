package com.gtnewhorizons.angelica.models;

import com.gtnewhorizons.angelica.compat.mojang.DefaultVertexFormat;
import com.gtnewhorizons.angelica.compat.mojang.VertexFormat;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Holds all the array offsets and bit-wise encoders/decoders for
 * packing/unpacking quad data in an array of integers.
 * All of this is implementation-specific - that's why it isn't a "helper" class.
 */
public abstract class EncodingFormat {
	private EncodingFormat() { }

	static final int HEADER_BITS = 0;
	static final int HEADER_FACE_NORMAL = 1;
	static final int HEADER_COLOR_INDEX = 2;
	static final int HEADER_TAG = 3;
	public static final int HEADER_STRIDE = 4;

	static final int VERTEX_X;
	static final int VERTEX_Y;
	static final int VERTEX_Z;
	static final int VERTEX_COLOR;
	static final int VERTEX_U;
	static final int VERTEX_V;
	static final int VERTEX_LIGHTMAP;
	static final int VERTEX_NORMAL;
	public static final int VERTEX_STRIDE;

	public static final int QUAD_STRIDE;
	public static final int QUAD_STRIDE_BYTES;
	public static final int TOTAL_STRIDE;

	static {
        // Upstream uses POSITION_COLOR_TEXTURE_LIGHT_NORMAL, but Nd uses POSITION_COLOR_TEXTURE_LIGHT_NORMAL_NOPAD
		final VertexFormat format = DefaultVertexFormat.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
		VERTEX_X = HEADER_STRIDE + 0;
		VERTEX_Y = HEADER_STRIDE + 1;
		VERTEX_Z = HEADER_STRIDE + 2;
		VERTEX_COLOR = HEADER_STRIDE + 3;
		VERTEX_U = HEADER_STRIDE + 4;
		VERTEX_V = VERTEX_U + 1;
		VERTEX_LIGHTMAP = HEADER_STRIDE + 6;
		VERTEX_NORMAL = HEADER_STRIDE + 7;
		VERTEX_STRIDE = format.getVertexSize();
		QUAD_STRIDE = VERTEX_STRIDE * 4;
		QUAD_STRIDE_BYTES = QUAD_STRIDE * 4;
		TOTAL_STRIDE = HEADER_STRIDE + QUAD_STRIDE;

		//Preconditions.checkState(VERTEX_STRIDE == QuadView.VANILLA_VERTEX_STRIDE, "Indigo vertex stride (%s) mismatched with rendering API (%s)", VERTEX_STRIDE, QuadView.VANILLA_VERTEX_STRIDE);
		//Preconditions.checkState(QUAD_STRIDE == QuadView.VANILLA_QUAD_STRIDE, "Indigo quad stride (%s) mismatched with rendering API (%s)", QUAD_STRIDE, QuadView.VANILLA_QUAD_STRIDE);
	}

	/** used for quick clearing of quad buffers. */
	static final int[] EMPTY = new int[TOTAL_STRIDE];

    // Default 7
	private static final int DIRECTION_MASK = MathHelper.roundUpToPowerOfTwo(ModelHelper.NULL_FACE_ID) - 1;
    // Default 3
	private static final int DIRECTION_BIT_COUNT = Integer.bitCount(DIRECTION_MASK);
	private static final int CULL_SHIFT = 0;
	private static final int CULL_INVERSE_MASK = ~(DIRECTION_MASK << CULL_SHIFT);
	private static final int LIGHT_SHIFT = CULL_SHIFT + DIRECTION_BIT_COUNT;
	private static final int LIGHT_INVERSE_MASK = ~(DIRECTION_MASK << LIGHT_SHIFT);
	private static final int NORMALS_SHIFT = LIGHT_SHIFT + DIRECTION_BIT_COUNT;
	private static final int NORMALS_COUNT = 4;
    // Lowest 4 bits
	private static final int NORMALS_MASK = (1 << NORMALS_COUNT) - 1;
    // -------- -------- ------NN NN------
	private static final int NORMALS_INVERSE_MASK = ~(NORMALS_MASK << NORMALS_SHIFT);
	private static final int GEOMETRY_SHIFT = NORMALS_SHIFT + NORMALS_COUNT;
	private static final int GEOMETRY_MASK = (1 << GeometryHelper.FLAG_BIT_COUNT) - 1;
	private static final int GEOMETRY_INVERSE_MASK = ~(GEOMETRY_MASK << GEOMETRY_SHIFT);
	private static final int MATERIAL_SHIFT = GEOMETRY_SHIFT + GeometryHelper.FLAG_BIT_COUNT;
	//private static final int MATERIAL_MASK = MathHelper.roundUpToPowerOfTwo(RenderMaterialImpl.VALUE_COUNT) - 1;
	//private static final int MATERIAL_BIT_COUNT = Integer.bitCount(MATERIAL_MASK);
	//private static final int MATERIAL_INVERSE_MASK = ~(MATERIAL_MASK << MATERIAL_SHIFT);

	/*static {
		Preconditions.checkArgument(MATERIAL_SHIFT + MATERIAL_BIT_COUNT <= 32, "Indigo header encoding bit count (%s) exceeds integer bit length)", TOTAL_STRIDE);
	}*/

	static ForgeDirection cullFace(int bits) {
		return ModelHelper.faceFromIndex((bits >>> CULL_SHIFT) & DIRECTION_MASK);
	}

	static int cullFace(int bits, ForgeDirection face) {
		return (bits & CULL_INVERSE_MASK) | (ModelHelper.toFaceIndex(face) << CULL_SHIFT);
	}

	static ForgeDirection lightFace(int bits) {
		return ModelHelper.faceFromIndex((bits >>> LIGHT_SHIFT) & DIRECTION_MASK);
	}

	static int lightFace(int bits, ForgeDirection face) {
		return (bits & LIGHT_INVERSE_MASK) | (ModelHelper.toFaceIndex(face) << LIGHT_SHIFT);
	}

	/** indicate if vertex normal has been set - bits correspond to vertex ordinals. */
	static int normalFlags(int bits) {
		return (bits >>> NORMALS_SHIFT) & NORMALS_MASK;
	}

    /**
     * I believe this merges a set of flags with the given normal flags.
     */
	public static int normalFlags(int bits, int normalFlags) {

        // (normalFlags & NORMALS_MASK) extracts the bottom four bits
        // << NORMALS_SHIFT shifts it up to the right spot
        // (bits & NORMALS_INVERSE_MASK) extracts normals from the bits
        // The two sets are ORd together and returned
		return (bits & NORMALS_INVERSE_MASK) | ((normalFlags & NORMALS_MASK) << NORMALS_SHIFT);
	}

	static int geometryFlags(int bits) {
		return (bits >>> GEOMETRY_SHIFT) & GEOMETRY_MASK;
	}

	static int geometryFlags(int bits, int geometryFlags) {
		return (bits & GEOMETRY_INVERSE_MASK) | ((geometryFlags & GEOMETRY_MASK) << GEOMETRY_SHIFT);
	}

	/*static RenderMaterialImpl material(int bits) {
		return RenderMaterialImpl.byIndex((bits >>> MATERIAL_SHIFT) & MATERIAL_MASK);
	}

	public static int material(int bits, RenderMaterialImpl material) {
		return (bits & MATERIAL_INVERSE_MASK) | (material.index() << MATERIAL_SHIFT);
	}*/
}