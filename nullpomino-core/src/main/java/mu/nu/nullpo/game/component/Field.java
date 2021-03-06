/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package mu.nu.nullpo.game.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;

import org.apache.log4j.Logger;

/**
 * Gamefield
 */
public class Field implements Serializable {
	/** Log */
	static Logger log = Logger.getLogger(Field.class);

	/** Serial version ID */
	private static final long serialVersionUID = 7745183278794213487L;

	/**  default The width of the */
	public static final int DEFAULT_WIDTH = 10;

	/**  default The height of the */
	public static final int DEFAULT_HEIGHT = 20;

	/**  default The height of the unseen parts of */
	public static final int DEFAULT_HIDDEN_HEIGHT = 3;

	/** Attributes of the coordinate (Usually) */
	public static final int COORD_NORMAL = 0;

	/** Attributes of the coordinate (Invisible part) */
	public static final int COORD_HIDDEN = 1;

	/** Attributes of the coordinate (Was placedBlockDisappears) */
	public static final int COORD_VANISH = 2;

	/** Attributes of the coordinate (Wall) */
	public static final int COORD_WALL = 3;

	/** fieldThe width of the */
	protected int width;

	/** Field height */
	protected int height;

	/** fieldThe height of the invisible part of the above */
	protected int hidden_height;

	/*
	 *	Oct. 6th, 2010: Changed block_field[][] and block_hidden[][] to [row][column] format,
	 *  and updated all relevant functions to match.
	 * 	This should facilitate referencing rows within the field.
	 *	It appears the unfinished flipVertical() was written assuming this was possible,
	 *	and it would greatly ease some of the work I'm currently doing without having
	 *	any visible effects outside this function.
	 *				-Kitaru
	 */

	/** fieldOfBlock */
	protected Block[][] block_field;

	/** fieldInvisible on the part ofBlock */
	protected Block[][] block_hidden;

	/** Line clear flag */
	protected boolean[] lineflag_field;

	/** I do not look the part ofLine clear flag */
	protected boolean[] lineflag_hidden;

	/** HURRY UPOf groundcount */
	protected int hurryupFloorLines;

	/** Presence or absence of a ceiling */
	public boolean ceiling;

	/** Number of total blocks above minimum required in color clears */
	public int colorClearExtraCount;

	/** Number of different colors in simultaneous color clears */
	public int colorsCleared;

	/** Number of gems cleared in last color or line color clear */
	public int gemsCleared;

	/** Number of garbage blocks cleared in last color clear */
	public int garbageCleared;

	/** List of colors of lines cleared in most recent line color clear */
	public ArrayList<Integer> lineColorsCleared;

	/** List of last rows cleared in most recent horizontal line clear. */
	public ArrayList<Block[]> lastLinesCleared;

	/** Used for TGM garbage, can later be extended to all types */
	//public ArrayList<Block[]> pendingGarbage;

	/**
	 * With parametersConstructor
	 * @param w fieldThe width of the
	 * @param h Field height
	 * @param hh fieldThe height of the invisible part of the above
	 */
	public Field(int w, int h, int hh) {
		width = w;
		height = h;
		hidden_height = hh;
		ceiling = false;

		reset();
	}

	/**
	 * With parametersConstructor
	 * @param w fieldThe width of the
	 * @param h Field height
	 * @param hh fieldThe height of the invisible part of the above
	 * @param c Presence or absence of a ceiling
	 */
	public Field(int w, int h, int hh, boolean c) {
		this(w, h, hh);
		ceiling = c;
	}

	/**
	 * Default constructor
	 */
	public Field() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_HIDDEN_HEIGHT);
	}

	/**
	 * Copy constructor
	 * @param f Copy source
	 */
	public Field(Field f) {
		copy(f);
	}

	/**
	 * Called at initialization
	 */
	public void reset() {
		block_field = new Block[height][width];
		block_hidden = new Block[hidden_height][width];
		lineflag_field = new boolean[height];
		lineflag_hidden = new boolean[hidden_height];
		hurryupFloorLines = 0;

		colorClearExtraCount = 0;
		colorsCleared = 0;
		gemsCleared = 0;
		lineColorsCleared = null;
		lastLinesCleared = null;
		garbageCleared = 0;

		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				block_field[j][i] = new Block();
			}
			for(int j = 0; j < hidden_height; j++) {
				block_hidden[j][i] = new Block();
			}
		}
	}

	/**
	 * AnotherFieldCopied from the
	 * @param f Copy source
	 */
	public void copy(Field f) {
		width = f.width;
		height = f.height;
		hidden_height = f.hidden_height;
		ceiling = f.ceiling;

		block_field = new Block[height][width];
		block_hidden = new Block[hidden_height][width];
		lineflag_field = new boolean[height];
		lineflag_hidden = new boolean[hidden_height];
		hurryupFloorLines = f.hurryupFloorLines;

		colorClearExtraCount = f.colorClearExtraCount;
		colorsCleared = f.colorsCleared;
		gemsCleared = f.gemsCleared = 0;
		lineColorsCleared = f.lineColorsCleared;
		lastLinesCleared = f.lastLinesCleared;
		garbageCleared = f.garbageCleared;

		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				block_field[j][i] = new Block(f.getBlock(i, j));
			}
			for(int j = 0; j < hidden_height; j++) {
				block_hidden[j][i] = new Block(f.getBlock(i, -j-1));
			}
		}
	}

	/**
	 * Stored in the property set
	 * @param p Property Set
	 * @param id AppropriateID
	 */
	public void writeProperty(CustomProperties p, int id) {
		for(int i = 0; i < height; i++) {
			String mapStr = "";

			for(int j = 0; j < width; j++) {
				mapStr += String.valueOf(getBlockColor(j, i));
				if(j < width - 1) mapStr += ",";
			}

			p.setProperty(id + ".field.map." + i, mapStr);
		}
	}

	/**
	 * Read from the property set
	 * @param p Property Set
	 * @param id AppropriateID
	 */
	public void readProperty(CustomProperties p, int id) {
		for(int i = 0; i < height; i++) {
			String mapStr = p.getProperty(id + ".field.map." + i, "");
			String[] mapArray = mapStr.split(",");

			for(int j = 0; j < mapArray.length; j++) {
				int blkColor = Block.BLOCK_COLOR_NONE;

				try {
					blkColor = Integer.parseInt(mapArray[j]);
				} catch (NumberFormatException e) {}

				setBlockColor(j, i, blkColor);

				if(getBlock(j, i) != null) {
					getBlock(j, i).elapsedFrames = -1;
				}
			}
		}
	}

	/**
	 * fieldGets the width of the
	 * @return fieldThe width of the
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Field heightGet the
	 * @return Field height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * fieldGets the height of the invisible part of the above
	 * @return fieldThe height of the invisible part of the above
	 */
	public int getHiddenHeight() {
		return hidden_height;
	}

	/**
	 * Gets the attributes of the specified coordinates
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Attributes of the coordinate
	 */
	public int getCoordAttribute(int x, int y) {
		// Ceiling
		if((y < 0) && (ceiling)) return COORD_WALL;

		// Wall
		if((x < 0) || (x >= width) || (y >= height)) return COORD_WALL;

		// Usually
		if((y >= 0) && (y < height)) return COORD_NORMAL;

		// Invisible part
		int y2 = (y * -1) - 1;
		if(y2 < hidden_height) return COORD_HIDDEN;

		// Was placedBlockDisappears
		return COORD_VANISH;
	}

	/**
	 * @param y height of the row in the field
	 * @return a reference to the row
	 */
	public Block[] getRow(int y) {
		try {
			return getRowE(y);
		} catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * @param y height of the row in the field
	 * @return a reference to the row
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Block[] getRowE(int y) throws ArrayIndexOutOfBoundsException {
		if(y >= 0) {
			try {
				return block_field[y];
			} catch(ArrayIndexOutOfBoundsException e) {
				throw e;
			}
		}
		// fieldOutside
		try {
			int y2 = (y * -1) - 1;
			return block_hidden[y2];
		} catch(ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}

	/**
	 * Is located at the specified coordinatesBlockGet the
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Is located at the specified coordinates successfulBlockObject, Failednull
	 */
	public Block getBlock(int x, int y) {
		try {
			return getBlockE(x, y);
		} catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Is located at the specified coordinatesBlockGet the (Exception sending failed)
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Is located at the specified coordinatesBlockObject
	 * @throws ArrayIndexOutOfBoundsException The specified coordinates out of range
	 */
	public Block getBlockE(int x, int y) throws ArrayIndexOutOfBoundsException {
		try {
			return getRowE(y)[x];
		} catch(ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}

	/**
	 * Set block to specific location
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param blk Block
	 * @return true if successful, false if failed
	 */
	public boolean setBlock(int x, int y, Block blk) {
		try {
			setBlockE(x, y, blk);
		} catch(ArrayIndexOutOfBoundsException e) {
			return false;
		} catch(NullPointerException e) {
			return false;	// There is a possible NPE here in avalanche modes
		}

		return true;
	}

	/**
	 * Set block to specific location (Throws exception when fails)
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param blk Block
	 * @throws ArrayIndexOutOfBoundsException When the coordinate is invalid
	 */
	public void setBlockE(int x, int y, Block blk) throws ArrayIndexOutOfBoundsException {
		try {
			getBlock(x,y).copy(blk);
		} catch(ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}

	/**
	 * Is located at the specified coordinatesBlock colorGet the
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Is located at the specified coordinatesBlock color (FailedBLOCK_COLOR_INVALID)
	 */
	public int getBlockColor(int x, int y) {
		try {
			return getBlockColorE(x, y);
		} catch(ArrayIndexOutOfBoundsException e) {
			return Block.BLOCK_COLOR_INVALID;
		}
	}

	/**
	 * Is located at the specified coordinatesBlock colorGet the
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param gemSame If true, a gem block will return the color of the corresponding normal block.
	 * @return Is located at the specified coordinatesBlock color (FailedBLOCK_COLOR_INVALID)
	 */
	public int getBlockColor(int x, int y, boolean gemSame) {
		return Block.gemToNormalColor(getBlockColor(x, y));
	}

	/**
	 * Is located at the specified coordinatesBlock colorGet the (Exception sending failed)
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Is located at the specified coordinatesBlock color
	 * @throws ArrayIndexOutOfBoundsException The specified coordinates out of range
	 */
	public int getBlockColorE(int x, int y) throws ArrayIndexOutOfBoundsException {
		try {
			return getBlockE(x,y).color;
		} catch(ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}

	/**
	 * Is located at the specified coordinatesBlock colorChange
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param c Color
	 * @return true if successful, false if failed
	 */
	public boolean setBlockColor(int x, int y, int c) {
		try {
			setBlockColorE(x, y, c);
		} catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}

		return true;
	}

	/**
	 * Is located at the specified coordinatesBlock colorChange (Exception sending failed)
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param c Color
	 * @throws ArrayIndexOutOfBoundsException The specified coordinates out of range
	 */
	public void setBlockColorE(int x, int y, int c) throws ArrayIndexOutOfBoundsException {
		try {
			getBlockE(x,y).color = c;
		} catch(ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}

	/**
	 * Line clear flagGet the
	 * @param y Y-coordinate
	 * @return If the column to disappeartrue, Otherwise (If the coordinates are out of range) orfalse
	 */
	public boolean getLineFlag(int y) {
		// fieldIn
		if(y >= 0) {
			try {
				return lineflag_field[y];
			} catch(ArrayIndexOutOfBoundsException e) {
				return false;
			}
		}

		// fieldOutside
		int y2 = (y * -1) - 1;

		try {
			return lineflag_hidden[y2];
		} catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	/**
	 * Is located at the specified coordinatesBlockDetermine whether the space is
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Is located at the specified coordinatesBlockIf space istrue (In the case of a specified coordinate is out of rangetrue)
	 */
	public boolean getBlockEmpty(int x, int y) {
		try {
			return getBlockEmptyE(x,y);
		} catch(ArrayIndexOutOfBoundsException e) {
			return true;
		}
	}

	/**
	 * Is located at the specified coordinatesBlockDetermine whether the space is (If the coordinates are outside the specified rangefalse)
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Is located at the specified coordinatesBlockIf space istrue (If the coordinates are outside the specified rangefalse)
	 */
	public boolean getBlockEmptyF(int x, int y) {
		try {
			return getBlockEmptyE(x,y);
		} catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	/**
	 * Is located at the specified coordinatesBlockDetermine whether the space is (Exception sending failed)
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Is located at the specified coordinatesBlockIf space istrue
	 * @throws ArrayIndexOutOfBoundsException The specified coordinates out of range
	 */
	public boolean getBlockEmptyE(int x, int y) throws ArrayIndexOutOfBoundsException {
		try {
			return getBlockE(x,y).isEmpty();
		} catch(ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}

	/**
	 * Line clear flagGet the (Exception sending failed)
	 * @param y Y-coordinate
	 * @return If the column to disappeartrue, Otherwisefalse
	 * @throws ArrayIndexOutOfBoundsException The specified coordinates out of range
	 */
	public boolean getLineFlagE(int y) throws ArrayIndexOutOfBoundsException {
		// fieldIn
		if(y >= 0) {
			try {
				return lineflag_field[y];
			} catch(ArrayIndexOutOfBoundsException e) {
				throw e;
			}
		}

		// fieldOutside
		try {
			int y2 = (y * -1) - 1;
			return lineflag_hidden[y2];
		} catch(ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}

	/**
	 * Line clear flagSet the
	 * @param y Y-coordinate
	 * @param flag SetLine clear flag
	 * @return true if successful, false if failed
	 */
	public boolean setLineFlag(int y, boolean flag) {
		// fieldIn
		if(y >= 0) {
			try {
				lineflag_field[y] = flag;
			} catch(ArrayIndexOutOfBoundsException e) {
				return false;
			}
		}
		// fieldOutside
		else {
			int y2 = (y * -1) - 1;

			try {
				lineflag_hidden[y2] = flag;
			} catch(ArrayIndexOutOfBoundsException e) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Line clear flagSet the (Exception sending failed)
	 * @param y Y-coordinate
	 * @param flag SetLine clear flag
	 * @throws ArrayIndexOutOfBoundsException The specified coordinates out of range
	 */
	public void setLineFlagE(int y, boolean flag) throws ArrayIndexOutOfBoundsException {
		// fieldIn
		if(y >= 0) {
			try {
				lineflag_field[y] = flag;
			} catch(ArrayIndexOutOfBoundsException e) {
				throw e;
			}
		}
		// fieldOutside
		else {
			try {
				int y2 = (y * -1) - 1;
				lineflag_hidden[y2] = flag;
			} catch(ArrayIndexOutOfBoundsException e) {
				throw e;
			}
		}
	}

	/**
	 * Line clear check
	 * @return VanishLinescount
	 */
	public int checkLine() {
		int lines = 0;

		if (lastLinesCleared == null){
			lastLinesCleared = new ArrayList<Block[]>();
		}
		lastLinesCleared.clear();

		Block[] row = new Block[width];

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			boolean flag = true;

			for(int j = 0; j < width; j++) {
				row[j] = new Block(getBlock(j, i));
				if((getBlockEmpty(j, i) == true) || (getBlock(j, i).getAttribute(Block.BLOCK_ATTRIBUTE_WALL) == true)) {
					flag = false;
					break;
				}
			}

			setLineFlag(i, flag);

			if(flag) {
				lines++;
				lastLinesCleared.add(row);

				for(int j = 0; j < width; j++) {
					getBlock(j, i).setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
				}
			}
		}

		return lines;
	}

	/**
	 * Line clear check  (Elimination flagI will not or settings)
	 * @return VanishLinescount
	 */
	public int checkLineNoFlag() {
		int lines = 0;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			boolean flag = true;

			for(int j = 0; j < width; j++) {
				if((getBlockEmpty(j, i) == true) || (getBlock(j, i).getAttribute(Block.BLOCK_ATTRIBUTE_WALL) == true)) {
					flag = false;
					break;
				}
			}

			if(flag) {
				lines++;
			}
		}

		return lines;
	}

	/**
	 * LinesDisappear
	 * @return Had disappearedLinescount
	 */
	public int clearLine() {
		int lines = 0;

		// fieldIn
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i)) {
				lines++;

				for(int j = 0; j < width; j++) {
					Block b = getBlock(j, i);
					if (b == null)
						continue;
					if (b.hard > 0)
					{
						b.hard--;
						setLineFlag(i, false);
					}
					else
						setBlockColor(j, i, Block.BLOCK_COLOR_NONE);
				}
			}
		}

		// Had disappearedLinesThe top and bottom of theBlockClear the binding of
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i - 1)) {
				for(int j = 0; j < width; j++) {
					Block blk = getBlock(j, i);

					if(blk != null && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
						setBlockLinkBroken(j, i);
					}
				}
			}
			if(getLineFlag(i + 1)) {
				for(int j = 0; j < width; j++) {
					Block blk = getBlock(j, i);

					if(blk != null && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
						setBlockLinkBroken(j, i);
					}
				}
			}
		}

		return lines;
	}

	/**
	 * Was on theBlockDown to the bottom all the
	 * @return Had disappearedLinescount
	 */
	public int downFloatingBlocks() {
		int lines = 0;
		int y = getHeightWithoutHurryupFloor() - 1;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(y)) {
				lines++;

				// BlockA1Copied from the rows above
				for(int k = y; k > (hidden_height * -1); k--) {
					for(int l = 0; l < width; l++) {
						Block blk = getBlock(l, k - 1);
						if(blk == null) blk = new Block();
						setBlock(l, k, blk);
						setLineFlag(k, getLineFlag(k - 1));
					}
				}

				// Blank to the top
				for(int l = 0; l < width; l++) {
					Block blk = new Block();
					setBlock(l, (hidden_height * -1), blk);
				}
				setLineFlag((hidden_height * -1), false);
			} else {
				y--;
			}
		}

		return lines;
	}

	/**
	 * Was on theBlockA1Only step down
	 */
	public void downFloatingBlocksSingleLine() {
		int y = getHeightWithoutHurryupFloor() - 1;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(y)) {
				// BlockA1Copied from the rows above
				for(int k = y; k > (hidden_height * -1); k--) {
					for(int l = 0; l < width; l++) {
						Block blk = getBlock(l, k - 1);
						if(blk == null) blk = new Block();
						setBlock(l, k, blk);
						setLineFlag(k, getLineFlag(k - 1));
					}
				}

				// Blank to the top
				for(int l = 0; l < width; l++) {
					Block blk = new Block();
					setBlock(l, (hidden_height * -1), blk);
				}
				setLineFlag((hidden_height * -1), false);
				return;
			} else {
				y--;
			}
		}
	}

	/**
	 * VanishLinescountAcountObtained
	 * @return Linescount
	 */
	public int getLines() {
		int lines = 0;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i)) lines++;
		}

		return lines;
	}

	/**
	 * All clearIf it&#39;s the casetrue
	 * @return All clearIf it&#39;s the casetrue
	 */
	public boolean isEmpty() {
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i) == false) {
				for(int j = 0; j < width; j++) {
					if(getBlockEmpty(j, i) == false) {
						Block b = getBlock(j, i);
						if (!b.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE))
							return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Check if specified line is completely empty
	 * @param y Y coord
	 * @return <code>true</code> if the specified line is completely empty, <code>false</code> otherwise.
	 */
	public boolean isEmptyLine(int y) {
		for(int x = 0; x < width; x++) {
			if(getBlockEmpty(x, y) == false) return false;
		}
		return true;
	}

	/**
	 * T-SpinIf it was the terrain to betrue
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param big BigWhether
	 * @return T-SpinIf it was the terrain to betrue
	 */
	public boolean isTSpinSpot(int x, int y, boolean big) {
		// Sets the coordinates for determining the relative
		int[] tx = new int[4];
		int[] ty = new int[4];

		if(big == true) {
			tx[0] = 1;
			ty[0] = 1;
			tx[1] = 4;
			ty[1] = 1;
			tx[2] = 1;
			ty[2] = 4;
			tx[3] = 4;
			ty[3] = 4;
		} else {
			tx[0] = 0;
			ty[0] = 0;
			tx[1] = 2;
			ty[1] = 0;
			tx[2] = 0;
			ty[2] = 2;
			tx[3] = 2;
			ty[3] = 2;
		}

		// Judgment
		int count = 0;

		for(int i = 0; i < tx.length; i++) {
			if(getBlockColor(x + tx[i], y + ty[i]) != Block.BLOCK_COLOR_NONE) count++;
		}

		if(count >= 3) return true;

		return false;
	}

	/**
	 * T-SpinIf it was a hole I cantrue
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param big BigWhether
	 * @return T-SpinIf it was a hole I cantrue
	 */
	public boolean isTSlot(int x, int y, boolean big) {
		// I wonder if the central buried
		if(big == true) {
			if(!getBlockEmptyF(x + 2, y + 2)) {
				return false;
			}
		} else {
			//□ □ □ ※ ※ ※ □ □ □ □
			//□ □ □ ★ ※ □ □ □ □ □
			//□ □ □ ※ ※ ※ □ □ □ □
			//□ □ □ ○ ※ ○ □ □ □ □

			if(!getBlockEmptyF(x + 1, y + 0)) return false;
			if(!getBlockEmptyF(x + 1, y + 1)) return false;
			if(!getBlockEmptyF(x + 1, y + 2)) return false;

			if(!getBlockEmptyF(x + 0, y + 1)) return false;
			if(!getBlockEmptyF(x + 2, y + 1)) return false;

			if(!getBlockEmptyF(x + 1, y - 1)) return false;
		}

		// Sets the coordinates for determining the relative
		int[] tx = new int[4];
		int[] ty = new int[4];

		if(big == true) {
			tx[0] = 1;
			ty[0] = 1;
			tx[1] = 4;
			ty[1] = 1;
			tx[2] = 1;
			ty[2] = 4;
			tx[3] = 4;
			ty[3] = 4;
		} else {
			tx[0] = 0;
			ty[0] = 0;
			tx[1] = 2;
			ty[1] = 0;
			tx[2] = 0;
			ty[2] = 2;
			tx[3] = 2;
			ty[3] = 2;
		}

		// Judgment
		int count = 0;

		for(int i = 0; i < tx.length; i++) {
			if(getBlockColor(x + tx[i], y + ty[i]) != Block.BLOCK_COLOR_NONE) count++;
		}

		if(count == 3) return true;

		return false;
	}

	/**
	 * T-SpinFind out what pieces I can have holes
	 * @param big BigIf it&#39;s the casetrue
	 * @return T-SpinOf holes I cancount
	 */
	public int getHowManyTSlot(boolean big) {
		int result = 0;

		for(int j = 0; j < width; j++) {
			for(int i = 0; i < getHeightWithoutHurryupFloor() - 2; i++) {
				if(getLineFlag(i) == false) {
					if(isTSlot(j, i, big)) {
						result++;
					}
				}
			}
		}

		return result;
	}

	public int getHowManyBlocksCovered() {
		int blocksCovered = 0;

		for(int j = 0; j < width; j++) {

            int highestBlockY=getHighestBlockY(j);
			for(int i = highestBlockY; i < getHeightWithoutHurryupFloor(); i++) {
				if(getLineFlag(i) == false) {

					if( getBlockEmpty(j, i)) {
					blocksCovered++;
					}

				}
			}
		}

		return blocksCovered;
	}
	/**
	 * T-SpinI disappearLinescountReturns
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param big BigWhether(Not supported)
	 * @return T-SpinI disappearLinescount(T-SpinFor example, if not the0)
	 */
	public int getTSlotLineClear(int x, int y, boolean big) {
		if(!isTSlot(x, y, big)) return 0;

		boolean[] lineflag = new boolean[2];
		lineflag[0] = lineflag[1] = true;

		for(int j = 0; j < width; j++) {
			for(int i = 0; i < 2; i++) {
				//■ ■ ■ ★ ※ ■ ■ ■ ■ ■
				//□ □ □ ※ ※ ※ □ □ □ □
				//□ □ □ ○ ※ ○ □ □ □ □
				if((j < x) || (j >= x + 3)) {
					if(getBlockEmptyF(j, y + 1 + i) == true) {
						lineflag[i] = false;
					}
				}
			}
		}

		int lines = 0;
		for(int i = 0; i < lineflag.length; i++) {
			if(lineflag[i]) lines++;
		}

		return lines;
	}

	/**
	 * T-SpinI disappearLinescountReturns(fieldWhole)
	 * @param big BigWhether(Not supported)
	 * @return T-SpinI disappearLinescount(T-SpinFor example, if not the0)
	 */
	public int getTSlotLineClearAll(boolean big) {
		int result = 0;

		for(int j = 0; j < width; j++) {
			for(int i = 0; i < getHeightWithoutHurryupFloor() - 2; i++) {
				if(getLineFlag(i) == false) {
					result += getTSlotLineClear(j, i, big);
				}
			}
		}

		return result;
	}

	/**
	 * T-SpinI disappearLinescountReturns(fieldWhole)
	 * @param big BigWhether(Not supported)
	 * @param minimum LowestLinescount(2When youT-Spin DoubleOnly sensitive to the)
	 * @return T-SpinI disappearLinescount(T-SpinOr if it is notminimumI do not meet theLinesEtc.0)
	 */
	public int getTSlotLineClearAll(boolean big, int minimum) {
		int result = 0;

		for(int j = 0; j < width; j++) {
			for(int i = 0; i < getHeightWithoutHurryupFloor() - 2; i++) {
				if(getLineFlag(i) == false) {
					int temp = getTSlotLineClear(j, i, big);

					if(temp >= minimum)
						result += temp;
				}
			}
		}

		return result;
	}

	/**
	 * fieldWhat in the number ofBlockExamine whether there is
	 * @return fieldAre withinBlockOfcount
	 */
	public int getHowManyBlocks() {
		int count = 0;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i) == false) {
				for(int j = 0; j < width; j++) {
					if(!getBlockEmpty(j, i)) {
						count++;
					}
				}
			}
		}

		return count;
	}

	/**
	 * How many of the left-BlockI examine whether the side-by-side
	 * @return Are arranged from leftBlockThe totalcount
	 */
	public int getHowManyBlocksFromLeft() {
		int count = 0;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i) == false) {
				for(int j = 0; j < width; j++) {
					if(!getBlockEmpty(j, i)) {
						count++;
					} else {
						break;
					}
				}
			}
		}

		return count;
	}

	/**
	 * How many of the rightBlockI examine whether the side-by-side
	 * @return Side-by-side from the rightBlockThe totalcount
	 */
	public int getHowManyBlocksFromRight() {
		int count = 0;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i) == false) {
				for(int j = width - 1; j > 0; j--) {
					if(!getBlockEmpty(j, i)) {
						count++;
					} else {
						break;
					}
				}
			}
		}

		return count;
	}

	/**
	 * At the topBlockOfY-coordinateGet the
	 * @return At the topBlockOfY-coordinate
	 */
	public int getHighestBlockY() {
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i) == false) {
				for(int j = 0; j < width; j++) {
					if(!getBlockEmpty(j, i)) return i;
				}
			}
		}

		return height;
	}

	/**
	 * At the topBlockOfY-coordinateGet the (X-coordinateWe can specifyVersion)
	 * @param x X-coordinate
	 * @return At the topBlockOfY-coordinate
	 */
	public int getHighestBlockY(int x) {
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i) == false) {
				if(!getBlockEmpty(x, i)) return i;
			}
		}

		return height;
	}

	/**
	 * garbage blockFirst occurrence of theY-coordinateGet the
	 * @return garbage blockFirst occurrence of theY-coordinate
	 */
	public int getHighestGarbageBlockY() {
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i) == false) {
				for(int j = 0; j < width; j++) {
					if(!getBlockEmpty(j, i) && getBlock(j, i).getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE))
						return i;
				}
			}
		}

		return height;
	}

	/**
	 * I will see if there is a gap under the specified coordinates
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return If there is a gap under the specified coordinatestrue
	 */
	public boolean isHoleBelow(int x, int y) {
		if(!getBlockEmpty(x, y) && getBlockEmpty(x, y + 1)) return true;
		return false;
	}

	/**
	 * fieldThe gap in thecountExamine the
	 * @return fieldThe gap in thecount
	 */
	public int getHowManyHoles() {
		int hole = 0;
		boolean samehole = false;

		for(int j = 0; j < width; j++) {
			samehole = false;

			for(int i = getHighestBlockY(); i < getHeightWithoutHurryupFloor(); i++) {
				if(getLineFlag(i) == false) {
					if(isHoleBelow(j, i)) {
						samehole = true;
					} else if(samehole && getBlockEmpty(j, i)) {
						hole++;
					} else {
						samehole = false;
					}
				}
			}
		}

		return hole;
	}

	/**
	 * Many pieces on top of the gapBlockI find out that you have stacked
	 * @return Are stackedBlockOfcount
	 */
	public int getHowManyLidAboveHoles() {
		int blocks = 0;

		for(int j = 0; j < width; j++) {
			int count = 0;

			for(int i = getHighestBlockY(); i < getHeightWithoutHurryupFloor() - 1; i++) {
				if(getLineFlag(i) == false) {
					if(isHoleBelow(j, i)) {
						count++;
						blocks += count;
						count = 0;
					} else if(!getBlockEmpty(j, i)) {
						count++;
					}
				}
			}
		}

		return blocks;
	}

	/**
	 * Every valley (■ ■ which returns the total depth of the) terrain that is (The return value is large enough to have many deep valleys)
	 * @return I shall be the sum of the depth of the valley all
	 */
	public int getTotalValleyDepth() {
		int depth = 0;

		for(int j = 0; j < width; j++) {
			int d = getValleyDepth(j);
			if(d >= 2) depth += d;
		}

		return depth;
	}

	/**
	 * IValley type is required (Depth3Or more)countReturns
	 * @return IValley type is requiredcount
	 */
	public int getTotalValleyNeedIPiece() {
		int count = 0;

		for(int j = 0; j < width; j++) {
			if(getValleyDepth(j) >= 3) count++;
		}

		return count;
	}

	/**
	 * Valley (■ ■ examine the depth of the) terrain that is
	 * @param x ExamineX-coordinate
	 * @return The depth of the valley (If you do not have0)
	 */
	public int getValleyDepth(int x) {
		int depth = 0;

		int highest = getHighestBlockY(x - 1);
		highest = Math.min(highest, getHighestBlockY(x));
		highest = Math.min(highest, getHighestBlockY(x + 1));

		for(int i = highest; i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i) == false) {
				if( (!getBlockEmptyF(x - 1, i) || (x <= 0)) && getBlockEmptyF(x, i) && (!getBlockEmptyF(x + 1, i) || (x >= width - 1)) )
					depth++;
			}
		}

		return depth;
	}

	/**
	 * fieldSlide on the entire
	 * @param lines shifting stagecount
	 */
	public void pushUp(int lines) {
		for(int k = 0; k < lines; k++) {
			for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor() - 1; i++) {
				// BlockA1Copy from the bottom stage
				for(int j = 0; j < width; j++) {
					Block blk = getBlock(j, i + 1);
					if(blk == null) blk = new Block();
					setBlock(j, i, blk);
					setLineFlag(i, getLineFlag(i + 1));
				}
			}

			// Blank to the bottom
			for(int j = 0; j < width; j++) {
				int y = getHeightWithoutHurryupFloor() - 1;
				setBlock(j, y, new Block());
				setLineFlag(y, false);
			}
		}
	}

	/**
	 * fieldThe entire1Staggered rows above
	 */
	public void pushUp() {
		pushUp(1);
	}

	/**
	 * fieldSlide down the entire
	 * @param lines shifting stagecount
	 */
	public void pushDown(int lines) {
		for(int k = 0; k < lines; k++) {
			for(int i = getHeightWithoutHurryupFloor() - 1; i > (hidden_height * -1); i--) {
				// BlockA1Copied from the rows above
				for(int j = 0; j < width; j++) {
					Block blk = getBlock(j, i - 1);
					if(blk == null) blk = new Block();
					setBlock(j, i, blk);
					setLineFlag(i, getLineFlag(i + 1));
				}
			}

			// Blank to the top
			for(int j = 0; j < width; j++) {
				setBlock(j, (hidden_height * -1), new Block());
				setLineFlag((hidden_height * -1), false);
			}
		}
	}

	/**
	 * fieldThe entire1Slide down the stage
	 */
	public void pushDown() {
		pushDown(1);
	}

	/**
	 * Cut the specified line(s) then push down all things above
	 * @param y Y coord
	 * @param lines Number of lines to cut
	 */
	public void cutLine(int y, int lines) {
		for(int k = 0; k < lines; k++) {
			for(int i = y; i > (hidden_height * -1); i--) {
				for(int j = 0; j < width; j++) {
					Block blk = getBlock(j, i - 1);
					if(blk == null) blk = new Block();
					setBlock(j, i, blk);
				}
				setLineFlag(i, getLineFlag(i + 1));
			}

			for(int j = 0; j < width; j++) {
				setBlock(j, (hidden_height * -1), new Block());
				setLineFlag((hidden_height * -1), false);
			}
		}
	}


	/**
	 * @return an ArrayList of rows representing the TGM attack of the last line clear action
	 * The TGM attack is the lines of the last line clear flipped vertically and without the blocks that caused it.
	 */
	public ArrayList<Block[]> getLastLinesAsTGMAttack(){
		ArrayList<Block[]> attack = new ArrayList<Block[]>();

		for(Block[] row : lastLinesCleared){
			Block[] row2 = new Block[getWidth()];
			for(int i = 0; i < getWidth(); i++){
				Block b = row[i];
				//Put an empty block if the original block was in the last commit to the field.
				if(b.getAttribute(Block.BLOCK_ATTRIBUTE_LAST_COMMIT)){
					row2[i] = new Block();
				}
				else{
					row2[i] = row[i];
				}
			}
			attack.add(0, row2);
		}

		return attack;
	}

	/**
	 * Holes1I only place opengarbage blockAdded to the bottom of the
	 * @param hole Hole position (-1If no hole)
	 * @param color garbage block color
	 * @param skin garbage blockPicture of
	 * @param attribute garbage blockAttributes
	 * @param lines Addgarbage blockOfLinescount
	 */
	public void addSingleHoleGarbage(int hole, int color, int skin, int attribute, int lines) {
		for(int k = 0; k < lines; k++) {
			pushUp(1);

			for(int j = 0; j < width; j++) {
				if(j != hole) {
					Block blk = new Block();
					blk.color = color;
					blk.skin = skin;
					blk.attribute = attribute;
					setBlock(j, getHeightWithoutHurryupFloor() - 1, blk);
				}
			}
		}
	}

	/**
	 * Add a single hole garbage (Attributes are automatically set)
	 * @param hole Hole position
	 * @param color Color
	 * @param skin Skin
	 * @param lines Number of garbage lines to add
	 */
	public void addSingleHoleGarbage(int hole, int color, int skin, int lines) {
		for(int k = 0; k < lines; k++) {
			pushUp(1);

			int y = getHeightWithoutHurryupFloor() - 1;

			for(int j = 0; j < width; j++) {
				if(j != hole) {
					Block blk = new Block();
					blk.color = color;
					blk.skin = skin;
					blk.attribute = Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE | Block.BLOCK_ATTRIBUTE_GARBAGE;
					setBlock(j, y, blk);
				}
			}

			// Set connections
			for(int j = 0; j < width; j++) {
				if(j != hole) {
					Block blk = getBlock(j, y);
						if(blk != null) {
						if(!getBlockEmpty(j - 1, y)) blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, true);
						if(!getBlockEmpty(j + 1, y)) blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, true);
					}
				}
			}
		}
	}

	/**
	 * BottommostLinesI copied the shape of thegarbage blockAdded to the bottom of the
	 * @param color garbage block color
	 * @param skin garbage blockPicture of
	 * @param attribute garbage blockAttributes
	 * @param lines Addgarbage blockOfLinescount
	 */
	public void addBottomCopyGarbage(int color, int skin, int attribute, int lines) {
		for(int k = 0; k < lines; k++) {
			pushUp(1);

			for(int j = 0; j < width; j++) {
				boolean empty = getBlockEmpty(j, height - 2);

				if(!empty) {
					Block blk = new Block();
					blk.color = color;
					blk.skin = skin;
					blk.attribute = attribute;
					setBlock(j, getHeightWithoutHurryupFloor() - 1, blk);
				}
			}
		}
	}

	/**
	 * 裏段位を取得
	 * (from NullpoMino Unofficial Expansion build 091309)
	 * @author Zircean
	 * @return Dan back
	 */
	public int getSecretGrade() {
		int holeLoc;
		int rows = 0;
		boolean rowCheck;

		for(int i = height - 1; i > 0; i--) {
			holeLoc = -Math.abs(i - (height / 2)) + (height / 2) - 1;
			if(getBlockEmpty(holeLoc, i) && !getBlockEmpty(holeLoc, i - 1)) {
				rowCheck = true;
				for(int j = 0; j < width; j++) {
					if(j != holeLoc && getBlockEmpty(j, i)) {
						rowCheck = false;
						break;
					}
				}
				if(rowCheck) {
					rows++;
				} else {
					break;
				}
			} else {
				break;
			}
		}

		return rows;
	}

	/**
	 * AllBlockChange the attributes of the
	 * @param attr I want to change the attributes
	 * @param status After the change state
	 */
	public void setAllAttribute(int attr, boolean status) {
		for(int i = (hidden_height * -1); i < height; i++) {
			for(int j = 0; j < width; j++) {
				Block blk = getBlock(j, i);

				if(blk != null) {
					blk.setAttribute(attr, status);
				}
			}
		}
	}

	/**
	 * AllBlockChange the pattern of
	 * @param skin Picture
	 */
	public void setAllSkin(int skin) {
		for(int i = (hidden_height * -1); i < height; i++) {
			for(int j = 0; j < width; j++) {
				Block blk = getBlock(j, i);

				if(blk != null) {
					blk.skin = skin;
				}
			}
		}
	}

	/**
	 * JewelBlockOfcountGet the
	 * @return JewelBlockOfcount
	 */
	public int getHowManyGems() {
		int gems = 0;
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			for(int j = 0; j < width; j++) {
				Block blk = getBlock(j, i);

				if((blk != null) && (blk.isGemBlock())) {
					gems++;
				}
			}
		}
		return gems;
	}

	/**
	 * JewelBlockDisappears or gets the number of
	 * @return gems disappearBlockOfcount
	 */
	public int getHowManyGemClears() {
		int gems = 0;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i)) {
				for(int j = 0; j < width; j++) {
					Block blk = getBlock(j, i);

					if((blk != null) && (blk.isGemBlock())) {
						gems++;
					}
				}
			}
		}

		return gems;
	}

	/**
	 * Checks for item blocks cleared
	 * @return A boolean array with true at each index for which an item block
	 *         of the corresponding ID number was cleared
	 */
	public boolean[] getItemClears() {
		boolean[] result = new boolean[Block.MAX_ITEM+1];

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			if(getLineFlag(i)) {
				for(int j = 0; j < width; j++) {
					Block blk = getBlock(j, i);

					if((blk != null) && (blk.item > 0) && blk.item <= Block.MAX_ITEM) {
						result[blk.item] = true;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Checks for 4x4 square formations and converts blocks to square blocks if needed.
	 * @return Number of square formations (index 0 is gold, index 1 is silver)
	 */
	public int[] checkForSquares() {
		int[] squares = {0,0};

		// Check for gold squares
		for (int i = (hidden_height * -1); i < (getHeightWithoutHurryupFloor() - 3); i++) {
			for (int j = 0; j < (width - 3); j++) {
				// rootBlk is the upper-left square
				Block rootBlk = getBlock(j, i);
				boolean squareCheck = false;

				/*
				 * id is the color of the top-left square: if it is a monosquare, every block in the
				 * 4x4 area will have this color.
				 */
				int id = Block.BLOCK_COLOR_NONE;
				if (!(rootBlk == null || rootBlk.isEmpty())) {
					id = rootBlk.color;
				}

				// This can't be a square if rootBlk doesn't exist or is part of another square.
				if (!(rootBlk == null || rootBlk.isEmpty() || rootBlk.isGoldSquareBlock() || rootBlk.isSilverSquareBlock())) {
					// A square is innocent until proven guilty.
					squareCheck = true;
					for (int k = 0; k < 4; k++) {
						for (int l = 0; l < 4; l++) {
							// blk is the current block
							Block blk = getBlock(j+l, i+k);
							/*
							 * Reasons why the entire area would not be a monosquare: this block does not exist,
							 * it is part of another square, it has been broken by line clears, is a garbage
							 * block, is not the same color as id, or has connections outside the area.
							 */
							if (blk == null || blk.isEmpty() || blk.isGoldSquareBlock() || blk.isSilverSquareBlock() ||
									blk.getAttribute(Block.BLOCK_ATTRIBUTE_BROKEN) ||
									blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE) || blk.color != id ||
									(l == 0 && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) ||
									(l == 3 && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) ||
									(k == 0 && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) ||
									(k == 3 && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN))){
								squareCheck = false;
								break;
							}
						}
						if (!squareCheck) {
							break;
						}
					}
				}
				// We found a square! Set all the blocks equal to gold blocks.
				if (squareCheck) {
					squares[0]++;
					int[] squareX = new int[] {0, 1, 1, 2};
					int[] squareY = new int[] {0, 3, 3, 6};
					for (int k = 0; k < 4; k++) {
						for (int l = 0; l < 4; l++) {
							Block blk = getBlock(j+l, i+k);
							blk.color = Block.BLOCK_COLOR_SQUARE_GOLD_1 + squareX[l] + squareY[k];
							// For stylistic concerns, we attach all blocks in the square together.
							if (k > 0) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, true);
							}
							if (k < 3) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, true);
							}
							if (l > 0) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, true);
							}
							if (l < 3) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, true);
							}
						}
					}
				}
			}
		}
		// Check for silver squares
		for (int i = (hidden_height * -1); i < (getHeightWithoutHurryupFloor() - 3); i++) {
			for (int j = 0; j < (width - 3); j++) {
				Block rootBlk = getBlock(j, i);
				boolean squareCheck = false;
				// We don't have to check colors because this loop checks for multisquares.
				if (!(rootBlk == null || rootBlk.isEmpty() || rootBlk.isGoldSquareBlock() || rootBlk.isSilverSquareBlock())) {
					// A square is innocent until proven guilty
					squareCheck = true;
					for (int k = 0; k < 4; k++) {
						for (int l = 0; l < 4; l++) {
							Block blk = getBlock(j+l, i+k);
							// See above, but without the color checking.
							if (blk == null || blk.isEmpty() || blk.isGoldSquareBlock() || blk.isSilverSquareBlock() ||
									blk.getAttribute(Block.BLOCK_ATTRIBUTE_BROKEN) ||
									blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE) ||
									(l == 0 && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) ||
									(l == 3 && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) ||
									(k == 0 && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) ||
									(k == 3 && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN))){
								squareCheck = false;
								break;
							}
						}
						if (!squareCheck) {
							break;
						}
					}
				}
				// We found a square! Set all the blocks equal to silver blocks.
				if (squareCheck) {
					squares[1]++;
					int[] squareX = new int[] {0, 1, 1, 2};
					int[] squareY = new int[] {0, 3, 3, 6};
					for (int k = 0; k < 4; k++) {
						for (int l = 0; l < 4; l++) {
							Block blk = getBlock(j+l, i+k);
							blk.color = Block.BLOCK_COLOR_SQUARE_SILVER_1 + squareX[l] + squareY[k];
							if (k > 0) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, true);
							}
							if (k < 3) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, true);
							}
							if (l > 0) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, true);
							}
							if (l < 3) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, true);
							}
						}
					}
				}
			}
		}

		return squares;
	}

	/**
	 * Checks the lines that are currently being cleared to see how many strips of squares are present in them.
	 * @return +1 for every 1x4 strip of gold (index 0) or silver (index 1)
	 */
	public int[] getHowManySquareClears() {
		int[] squares = {0,0};
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			// Check the lines we are clearing.
			if (getLineFlag(i)) {
				for(int j = 0; j < width; j++) {
					Block blk = getBlock(j, i);

					// Silver blocks are worth 1, gold are worth 2, but not if they are garbage (avalanche)
					if (blk != null && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
						if (blk.isGoldSquareBlock()) {
							squares[0]++;
						} else if (blk.isSilverSquareBlock()) {
							squares[1]++;
						}
					}
				}
			}
		}
		// We have to divide the amount by 4 because it's based on 1x4 strips, not single blocks.
		squares[0] /= 4;
		squares[1] /= 4;

		return squares;
	}

	/**
	 * Clear line colors of sufficient size.
	 * @param size Minimum length of line for a clear
	 * @param diagonals <code>true</code> to check diagonals, <code>false</code> to check only vertical and horizontal
	 * @param gemSame <code>true</code> to check gem blocks
	 * @return Total number of blocks cleared.
	 */
	public int clearLineColor (int size, boolean diagonals, boolean gemSame)
	{
		int total = 0;
		Block b, bAdj;
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++)
			for(int j = 0; j < width; j++)
			{
				b = getBlock(j, i);
				if (b == null)
					continue;
				if (b.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE))
				{
					total++;
					if (b.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN))
					{
						bAdj = getBlock(j, i+1);
						if (bAdj != null)
						{
							bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
							bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
						}
					}
					if (b.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP))
					{
						bAdj = getBlock(j, i-1);
						if (bAdj != null)
						{
							bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
							bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
						}
					}
					if (b.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT))
					{
						bAdj = getBlock(j-1, i);
						if (bAdj != null)
						{
							bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
							bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
						}
					}
					if (b.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT))
					{
						bAdj = getBlock(j+1, i);
						if (bAdj != null)
						{
							bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
							bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
						}
					}
					setBlockColor(j, i, Block.BLOCK_COLOR_NONE);
				}
			}
		return total;
	}
	/**
	 * Check for line color clears of sufficient size.
	 * @param size Minimum length of line for a clear
	 * @param flag <code>true</code> to set BLOCK_ATTRIBUTE_ERASE to true on blocks to be cleared.
	 * @param diagonals <code>true</code> to check diagonals, <code>false</code> to check only vertical and horizontal
	 * @param gemSame <code>true</code> to check gem blocks
	 * @return Total number of blocks that would be cleared.
	 */
	public int checkLineColor (int size, boolean flag, boolean diagonals, boolean gemSame)
	{
		if (size < 1)
			return 0;
		if (flag)
		{
			setAllAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
			if (lineColorsCleared == null)
				lineColorsCleared = new ArrayList<Integer>();
			gemsCleared = 0;
		}
		int total = 0;
		int x, y, count, blockColor, lineColor;
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			for(int j = 0; j < width; j++) {
				lineColor = getBlockColor(j, i, gemSame);
				if (lineColor == Block.BLOCK_COLOR_NONE || lineColor == Block.BLOCK_COLOR_INVALID)
					continue;
				for (int dir = 0; dir < (diagonals ? 3 : 2); dir++) {
					blockColor = lineColor;
					x = j;
					y = i;
					count = 0;
					while (lineColor == blockColor)
					{
						count++;
						if (dir != 1)
							y++;
						if (dir != 0)
							x++;
						blockColor = getBlockColor(x, y, gemSame);
					}
					if (count < size)
						continue;
					total += count;
					if (!flag)
						continue;
					if (count == size)
						lineColorsCleared.add(new Integer(lineColor));
					x = j;
					y = i;
					blockColor = lineColor;
					while (lineColor == blockColor)
					{
						Block b = getBlock(x, y);
						if (b.hard > 0)
							b.hard--;
						else if (!b.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE))
						{
							if (b.isGemBlock())
								gemsCleared++;
							b.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
						}
						if (dir != 1)
							y++;
						if (dir != 0)
							x++;
						blockColor = getBlockColor(x, y, gemSame);
					}
				}
			}
		}
		return total;
	}
	/**
	 * Performs all color clears of sufficient size containing at least one gem block.
	 * @param size Minimum size of cluster for a clear
	 * @param garbageClear <code>true</code> to clear garbage blocks adjacent to cleared clusters
	 * @return Total number of blocks cleared.
	 */
	public int gemClearColor (int size, boolean garbageClear, boolean ignoreHidden)
	{
		Field temp = new Field(this);
		int total = 0;
		Block b;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			for(int j = 0; j < width; j++) {
				b = getBlock(j, i);
				if (b == null)
					continue;
				if (!b.isGemBlock())
					continue;
				int clear = temp.clearColor(j, i, false, garbageClear, true, ignoreHidden);
				if (clear >= size)
				{
					total += clear;
					clearColor(j, i, false, garbageClear, true, ignoreHidden);
				}
			}
		}
		return total;
	}
	public int gemClearColor (int size, boolean garbageClear)
	{
		return gemClearColor(size, garbageClear, false);
	}
	/**
	 * Performs all color clears of sufficient size.
	 * @param size Minimum size of cluster for a clear
	 * @param garbageClear <code>true</code> to clear garbage blocks adjacent to cleared clusters
	 * @param gemSame <code>true</code> to check gem blocks
	 * @return Total number of blocks cleared.
	 */
	public int clearColor (int size, boolean garbageClear, boolean gemSame, boolean ignoreHidden)
	{
		Field temp = new Field(this);
		int total = 0;
		for(int i = ignoreHidden ? 0 : (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			for(int j = 0; j < width; j++) {
				int clear = temp.clearColor(j, i, false, garbageClear, gemSame, ignoreHidden);
				if (clear >= size)
				{
					total += clear;
					clearColor(j, i, false, garbageClear, gemSame, ignoreHidden);
				}
			}
		}
		return total;
	}
	public int clearColor (int size, boolean garbageClear, boolean gemSame)
	{
		return clearColor(size, garbageClear, gemSame, false);
	}
	/**
	 * Clears the block at the given position as well as all adjacent blocks of
	 * the same color, and any garbage blocks adjacent to the group if garbageClear is true.
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param flag <code>true</code> to set BLOCK_ATTRIBUTE_ERASE to true on cleared blocks.
	 * @param garbageClear <code>true</code> to clear garbage blocks adjacent to cleared clusters
	 * @param gemSame <code>true</code> to check gem blocks
	 * @return The number of blocks cleared.
	 */
	public int clearColor (int x, int y, boolean flag, boolean garbageClear, boolean gemSame,
			boolean ignoreHidden)
	{
		int blockColor = getBlockColor(x, y, gemSame);
		if (blockColor == Block.BLOCK_COLOR_NONE || blockColor == Block.BLOCK_COLOR_INVALID)
			return 0;
		Block b = getBlock(x, y);
		if (b == null)
			return 0;
		if (b.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE))
			return 0;
		else
			return clearColor(x, y, blockColor, flag, garbageClear, gemSame, ignoreHidden);
	}
	/**
	 * Note: This method is private because calling it with a targetColor parameter
	 *       of BLOCK_COLOR_NONE or BLOCK_COLOR_INVALID may cause an infinite loop
	 *       and crash the game. This check is handled by the above public method
	 *       so as to avoid redundant checks.
	 */
	private int clearColor (int x, int y, int targetColor, boolean flag, boolean garbageClear,
			boolean gemSame, boolean ignoreHidden)
	{
		if (ignoreHidden && y < 0)
			return 0;
		int blockColor = getBlockColor(x, y, gemSame);
		if (blockColor == Block.BLOCK_COLOR_INVALID)
			return 0;
		Block b = getBlock(x, y);
		if (flag && b.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE))
			return 0;
		if (garbageClear && b.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)
				 && !b.getAttribute(Block.BLOCK_ATTRIBUTE_WALL))
		{
			if (flag)
			{
				b.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
				garbageCleared++;
			}
			else if (b.hard > 0)
				b.hard--;
			else
				setBlockColor(x, y, Block.BLOCK_COLOR_NONE);
		}
		if (blockColor != targetColor)
			return 0;
		if (flag)
			b.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
		else if (b.hard > 0)
			b.hard--;
		else
			setBlockColor(x, y, Block.BLOCK_COLOR_NONE);
		return 1 + clearColor(x+1, y, targetColor, flag, garbageClear, gemSame, ignoreHidden)
				 + clearColor(x-1, y, targetColor, flag, garbageClear, gemSame, ignoreHidden)
				 + clearColor(x, y+1, targetColor, flag, garbageClear, gemSame, ignoreHidden)
				 + clearColor(x, y-1, targetColor, flag, garbageClear, gemSame, ignoreHidden);
	}

	/**
	 * Clears all blocks of the same color
	 * @param targetColor The color to clear
	 * @param flag <code>true</code> to set BLOCK_ATTRIBUTE_ERASE to true on cleared blocks.
	 * @param gemSame <code>true</code> to check gem blocks
	 * @return The number of blocks cleared.
	 */
	public int allClearColor (int targetColor, boolean flag, boolean gemSame)
	{
		if (targetColor < 0)
			return 0;
		if (gemSame)
			targetColor = Block.gemToNormalColor(targetColor);
		int total = 0;
		for (int y = (-1 * hidden_height); y < height; y++)
			for (int x = 0; x < width; x++)
				if (getBlockColor(x, y, gemSame) == targetColor)
				{
					total++;
					if (flag)
						getBlock(x, y).setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
					else
						setBlockColor(x, y, Block.BLOCK_COLOR_NONE);
				}
		return total;
	}

	public boolean doCascadeGravity(GameEngine.LineGravity type) {
		setAllAttribute(Block.BLOCK_ATTRIBUTE_LAST_COMMIT, false);
		if (type == GameEngine.LineGravity.CASCADE_SLOW)
			return doCascadeSlow();
		else
			return doCascadeGravity();
	}

	/**
	 * Main routine for cascade gravity.
	 * @return <code>true</code> if something falls. <code>false</code> if nothing falls.
	 */
	public boolean doCascadeGravity() {
		boolean result = false;

		setAllAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, false);

		for(int i = (getHeightWithoutHurryupFloor() - 1); i >= (hidden_height * -1); i--) {
			for(int j = 0; j < width; j++) {
				Block blk = getBlock(j, i);

				if((blk != null) && !blk.isEmpty() && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY)) {
					boolean fall = true;
					checkBlockLink(j, i);

					for(int k = (getHeightWithoutHurryupFloor() - 1); k >= (hidden_height * -1); k--) {
						for(int l = 0; l < width; l++) {
							Block bTemp = getBlock(l, k);

							if( (bTemp != null) && !bTemp.isEmpty() &&
								bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK) && !bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL) )
							{
								Block bBelow = getBlock(l, k + 1);

								if( (getCoordAttribute(l, k + 1) == COORD_WALL) ||
									((bBelow != null) && !bBelow.isEmpty() && !bBelow.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK))
								  )
								{
									fall = false;
								}
							}
						}
					}

					if(fall) {
						result = true;
						for(int k = (getHeightWithoutHurryupFloor() - 1); k >= (hidden_height * -1); k--) {
							for(int l = 0; l < width; l++) {
								Block bTemp = getBlock(l, k);
								Block bBelow = getBlock(l, k + 1);

								if( (getCoordAttribute(l, k + 1) != COORD_WALL) &&
								    (bTemp != null) && !bTemp.isEmpty() && (bBelow != null) && bBelow.isEmpty() &&
								    bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK) && !bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL) )
								{
									bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
									bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, true);
									if (bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_IGNORE_BLOCKLINK))
									{
										bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
										bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
										bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
										bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
									}
									bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_LAST_COMMIT, true);
									setBlock(l, k + 1, bTemp);
									setBlock(l, k, new Block());
								}
							}
						}
					}
				}
			}
		}

		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		setAllAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, false);

		/*
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			setLineFlag(i, false);
		}
		*/

		return result;
	}

	/**
	 * Routine for cascade gravity which checks from the top down for a slower fall animation.
	 * @return <code>true</code> if something falls. <code>false</code> if nothing falls.
	 */
	public boolean doCascadeSlow() {
		boolean result = false;

		setAllAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, false);

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			for(int j = 0; j < width; j++) {
				Block blk = getBlock(j, i);

				if((blk != null) && !blk.isEmpty() && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY)) {
					boolean fall = true;
					checkBlockLink(j, i);

					for(int k = (getHeightWithoutHurryupFloor() - 1); k >= (hidden_height * -1); k--) {
						for(int l = 0; l < width; l++) {
							Block bTemp = getBlock(l, k);

							if( (bTemp != null) && !bTemp.isEmpty() &&
								bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK) && !bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL) )
							{
								Block bBelow = getBlock(l, k + 1);

								if( (getCoordAttribute(l, k + 1) == COORD_WALL) ||
									((bBelow != null) && !bBelow.isEmpty() && !bBelow.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)) )
								{
									fall = false;
								}
							}
						}
					}

					if(fall) {
						result = true;
						for(int k = (getHeightWithoutHurryupFloor() - 1); k >= (hidden_height * -1); k--) {
							for(int l = 0; l < width; l++) {
								Block bTemp = getBlock(l, k);
								Block bBelow = getBlock(l, k + 1);

								if( (getCoordAttribute(l, k + 1) != COORD_WALL) &&
								    (bTemp != null) && !bTemp.isEmpty() && (bBelow != null) && bBelow.isEmpty() &&
								    bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK) && !bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL) )
								{
									bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
									bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, true);
									if (bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_IGNORE_BLOCKLINK))
									{
										bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
										bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
										bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
										bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
									}
									bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_LAST_COMMIT, true);
									setBlock(l, k + 1, bTemp);
									setBlock(l, k, new Block());
								}
							}
						}
					}
				}
			}
		}

		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		setAllAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, false);

		return result;
	}

	/**
	 * Checks the connection of blocks and set "mark" to each block.
	 * @param x X coord
	 * @param y Y coord
	 */
	public void checkBlockLink(int x, int y) {
		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		checkBlockLinkSub(x, y);
	}

	/**
	 * Subroutine for checkBlockLink.
	 * @param x X coord
	 * @param y Y coord
	 */
	protected void checkBlockLinkSub(int x, int y) {
		Block blk = getBlock(x, y);
		if((blk != null) && !blk.isEmpty() && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)) {
			blk.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, true);

			if(!blk.getAttribute(Block.BLOCK_ATTRIBUTE_IGNORE_BLOCKLINK)) {
				if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) checkBlockLinkSub(x, y - 1);
				if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) checkBlockLinkSub(x, y + 1);
				if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) checkBlockLinkSub(x - 1, y);
				if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) checkBlockLinkSub(x + 1, y);
			}
		}
	}

	/**
	 * Checks the connection of blocks and set the "broken" flag to each block.
	 * It only affects to normal blocks. (ex. not square or gems)
	 * @param x X coord
	 * @param y Y coord
	 */
	public void setBlockLinkBroken(int x, int y) {
		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		setBlockLinkBrokenSub(x, y);
	}

	/**
	 * Subroutine for setBlockLinkBrokenSub.
	 * @param x X coord
	 * @param y Y coord
	 */
	protected void setBlockLinkBrokenSub(int x, int y) {
		Block blk = getBlock(x, y);
		if((blk != null) && !blk.isEmpty() && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK) && blk.isNormalBlock()) {
			blk.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, true);
			blk.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
			if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) setBlockLinkBrokenSub(x, y - 1);
			if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) setBlockLinkBrokenSub(x, y + 1);
			if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) setBlockLinkBrokenSub(x - 1, y);
			if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) setBlockLinkBrokenSub(x + 1, y);
		}
	}

	/**
	 * Checks the color of blocks and set the connection flags to each block.
	 */
	public void setBlockLinkByColor() {
		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			for(int j = 0; j < width; j++) {
				setBlockLinkByColor(j, i);
			}
		}
	}

	/**
	 * Checks the color of blocks and set the connection flags to each block.
	 * @param x X coord
	 * @param y Y coord
	 */
	public void setBlockLinkByColor(int x, int y) {
		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		setBlockLinkByColorSub(x, y);
	}

	/**
	 * Subroutine for setBlockLinkByColorSub.
	 * @param x X coord
	 * @param y Y coord
	 */
	protected void setBlockLinkByColorSub(int x, int y) {
		Block blk = getBlock(x, y);
		if((blk != null) && !blk.isEmpty() && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK) &&
		    !blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE) && blk.isNormalBlock())
		{
			blk.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, true);
			if(getBlockColor(x, y - 1) == blk.color) {
				blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, true);
				setBlockLinkByColorSub(x, y - 1);
			} else {
				blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
			}
			if(getBlockColor(x, y + 1) == blk.color) {
				blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, true);
				setBlockLinkByColorSub(x, y + 1);
			} else {
				blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
			}
			if(getBlockColor(x - 1, y) == blk.color) {
				blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, true);
				setBlockLinkByColorSub(x - 1, y);
			} else {
				blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
			}
			if(getBlockColor(x + 1, y) == blk.color) {
				blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, true);
				setBlockLinkByColorSub(x + 1, y);
			} else {
				blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
			}
		}
	}

	/**
	 * HURRY UPAdded to the bottom of the ground
	 * @param lines RaiseLinescount
	 * @param skin Picture of the ground
	 */
	public void addHurryupFloor(int lines, int skin) {
		for(int k = 0; k < lines; k++) {
			pushUp(1);

			for(int j = 0; j < width; j++) {
				Block blk = new Block();
				blk.color = Block.BLOCK_COLOR_GRAY;
				blk.skin = skin;
				blk.attribute = Block.BLOCK_ATTRIBUTE_WALL | Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE;
				setBlock(j, getHeightWithoutHurryupFloor() - 1, blk);
			}

			hurryupFloorLines++;
		}
	}

	/**
	 * HURRY UPWhat is the ground ofLinesI will see if there
	 * @return HURRY UPGroundLinesOfcount
	 */
	public int getHurryupFloorLines() {
		return hurryupFloorLines;
	}

	/**
	 * HURRY UPI except the groundField heightReturns
	 * @return HURRY UPI except the groundField height
	 */
	public int getHeightWithoutHurryupFloor() {
		return height - hurryupFloorLines;
	}

	/**
	 * @param row Row of blocks
	 * @return a String representing the row
	 */
	public String rowToString(Block[] row){
		String strResult = "";

		for(int x = 0; x < row.length; x++) {
			strResult += row[x];
		}

		return strResult;
	}

	/**
	 * fieldConverted to a string
	 * @return It was converted to a stringfield
	 */
	public String fieldToString() {
		String strResult = "";

		for(int i = getHeight() - 1; i >= Math.max(-1, getHighestBlockY()); i--) {
			strResult += rowToString(getRow(i));
		}

		// Closing0Remove the
		while(strResult.endsWith("0")) {
			strResult = strResult.substring(0, strResult.length() - 1);
		}

		return strResult;
	}

	public Block[] stringToRow(String str){
		return stringToRow(str, 0, false, false);
	}

	/**
	 * @param str String representing field state
	 * @param skin Block skin being used in this field
	 * @param isGarbage Row is a garbage row
	 * @param isWall Row is a wall (i.e. hurry-up rows)
	 * @return The row array
	 */
	public Block[] stringToRow(String str, int skin, boolean isGarbage, boolean isWall){
		Block[] row = new Block[getWidth()];
		for(int j = 0; j < getWidth(); j++) {

			int blkColor = Block.BLOCK_COLOR_NONE;

			/*
			 * NullNoname's original approach from the old stringToField:
			 * If a character outside the row string is referenced,
			 * default to an empty block by ignoring the exception.
			 */
			try {
				char c = str.charAt(j);
				blkColor = Block.charToBlockColor(c);
			} catch (Exception e) {}

			row[j] = new Block();
			row[j].color = blkColor;
			row[j].skin = skin;
			row[j].elapsedFrames = -1;
			row[j].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			row[j].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);

			if(isGarbage) { //TODO: This may need extension when garbage does not only sport one hole (i.e. TGM garbage)
				row[j].setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
			}
			if(isWall) {
				row[j].setAttribute(Block.BLOCK_ATTRIBUTE_WALL, true);
			}
		}

		return row;
	}


	/**
	 * Based on a stringfieldChange
	 * @param str String
	 */
	public void stringToField(String str) {
		stringToField(str, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Based on a stringfieldChange
	 * @param str String
	 * @param skin BlockPicture of
	 * @param highestGarbageY The highestgarbage blockThe position of the
	 * @param highestWallY The highestHurryupBlockThe position of the
	 */
	public void stringToField(String str, int skin, int highestGarbageY, int highestWallY) {
		for(int i = -1; i < getHeight(); i++) {
			int index = (getHeight() - 1 - i) * getWidth();
			/*
			 * Much like NullNoname's try/catch from the old stringToField that is now in stringToRow,
			 * we need to skip over substrings referenced outside the field string -- empty rows.
			 */
			try{
				String substr = str.substring(index, Math.min(str.length(), index+getWidth()));
				Block[] row = stringToRow(substr, skin, (i >= highestGarbageY), (i >= highestWallY));
				for(int j = 0; j < getWidth(); j++){
					setBlock(j, i, row[j]);
				}
			}
			catch(Exception e){
				for(int j = 0; j < getWidth(); j++){
					setBlock(j, i, new Block(Block.BLOCK_COLOR_NONE));
				}
			}
		}
	}

	/**
	 * @param row Row of blocks
	 * @return a String representing the row with attributes
	 */
	public String attrRowToString(Block[] row){
		String strResult = "";

		for(int x = 0; x < row.length; x++) {
			strResult += Integer.toString(row[x].color, 16) + "/";
			strResult += Integer.toString(row[x].attribute, 16) + ";";
		}

		return strResult;
	}

	/**
	 * Convert this field to a String with attributes
	 * @return a String representing the field with attributes
	 */
	public String attrFieldToString() {
		String strResult = "";

		for(int i = getHeight() - 1; i >= Math.max(-1, getHighestBlockY()); i--) {
			strResult += attrRowToString(getRow(i));
		}
		while(strResult.endsWith("0/0;")) {
			strResult = strResult.substring(0, strResult.length() - 4);
		}

		return strResult;
	}

	public Block[] attrStringToRow(String str, int skin) {
		return attrStringToRow(str.split(";"), skin);
	}

	public Block[] attrStringToRow(String[] strArray, int skin) {
		Block[] row = new Block[getWidth()];

		for(int j = 0; j < getWidth(); j++) {
			int blkColor = Block.BLOCK_COLOR_NONE;
			int attr = 0;

			try {
				String[] strSubArray = strArray[j].split("/");
				if(strSubArray.length > 0)
					blkColor = Integer.parseInt(strSubArray[0], 16);
				if(strSubArray.length > 1)
					attr = Integer.parseInt(strSubArray[1], 16);
			} catch (Exception e) {}

			row[j] = new Block();
			row[j].color = blkColor;
			row[j].skin = skin;
			row[j].elapsedFrames = -1;
			row[j].attribute = attr;
			row[j].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			row[j].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
		}

		return row;
	}

	public void attrStringToField(String str, int skin) {
		String[] strArray = str.split(";");

		for(int i = -1; i < getHeight(); i++) {
			int index = (getHeight() - 1 - i) * getWidth();

			try{
				String strTemp = "";
				String[] strArray2 = new String[getWidth()];
				for(int j = 0; j < getWidth(); j++){
					if(index + j < strArray.length)
						strArray2[j] = strArray[index + j];
					else
						strArray2[j] = "";

					strTemp += strArray2[j] + "/";
				}

				Block[] row = attrStringToRow(strArray2, skin);
				for(int j = 0; j < getWidth(); j++){
					setBlock(j, i, row[j]);
				}
			}
			catch(Exception e){
				for(int j = 0; j < getWidth(); j++){
					setBlock(j, i, new Block(Block.BLOCK_COLOR_NONE));
				}
			}
		}
	}

	/**
	 * fieldGets the string representation of the
	 */
	@Override
	public String toString() {
		String str = getClass().getName() + "@" + Integer.toHexString(hashCode()) + "\n";

		for(int i = (hidden_height * -1); i < height; i++) {
			str += String.format("%3d:", i);

			for(int j = 0; j < width; j++) {
				int color = getBlockColor(j, i);

				if(color < 0) {
					str += "*";
				} else if(color >= 10) {
					str += "+";
				} else {
					str += Integer.toString(color);
				}
			}

			str += "\n";
		}

		return str;
	}

	public int checkColor(int size, boolean flag, boolean garbageClear, boolean gemSame, boolean ignoreHidden) {
		Field temp = new Field(this);
		int total = 0;
		boolean[] colorsClearedArray = new boolean[7];
		if (flag)
		{
			setAllAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
			garbageCleared = 0;
			colorClearExtraCount = 0;
			colorsCleared = 0;
			for (int i = 0; i < 7; i++)
				colorsClearedArray[i] = false;
		}

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++)
		{
			for(int j = 0; j < width; j++)
			{
				int clear = temp.clearColor(j, i, false, garbageClear, gemSame, ignoreHidden);
				if (clear >= size)
				{
					total += clear;
					if (flag)
					{
						int blockColor = getBlockColor(j, i, gemSame);
						clearColor(j, i, true, garbageClear, gemSame, ignoreHidden);
						colorClearExtraCount += clear - size;
						if (blockColor >= 2 && blockColor <= 8)
							colorsClearedArray[blockColor-2] = true;
					}
				}
			}
		}
		if (flag)
			for (int i = 0; i < 7; i++)
				if (colorsClearedArray[i])
					colorsCleared++;
		return total;
	}

	public void garbageDrop(GameEngine engine, int drop, boolean big) {
		garbageDrop(engine, drop, big, 0, 0, -1, Block.BLOCK_COLOR_GRAY);
	}
	public void garbageDrop(GameEngine engine, int drop, boolean big, int hard) {
		garbageDrop(engine, drop, big, hard, 0, -1, Block.BLOCK_COLOR_GRAY);
	}
	public void garbageDrop(GameEngine engine, int drop, boolean big, int hard, int countdown) {
		garbageDrop(engine, drop, big, hard, countdown, -1, Block.BLOCK_COLOR_GRAY);
	}
	public void garbageDrop(GameEngine engine, int drop, boolean big, int hard, int countdown, int avoidColumn) {
		garbageDrop(engine, drop, big, hard, countdown, avoidColumn, Block.BLOCK_COLOR_GRAY);
	}
	public void garbageDrop(GameEngine engine, int drop, boolean big, int hard, int countdown, int avoidColumn, int color) {
		int y = -1 * hidden_height;
		int actualWidth = width;
		if (big)
			actualWidth >>= 1;
		int bigMove = big ? 2 : 1;
		while (drop >= actualWidth)
		{
			drop -= actualWidth;
			for (int x = 0; x < actualWidth; x+=bigMove)
				garbageDropPlace(x, y, big, hard, color, countdown);
			y+=bigMove;
		}
		if (drop == 0)
			return;
		boolean[] placeBlock = new boolean[actualWidth];
		int j;
		if (drop > (actualWidth>>1))
		{
			for (int x = 0; x < actualWidth; x++)
				placeBlock[x] = true;
			int start = actualWidth;
			if (avoidColumn >= 0 && avoidColumn < actualWidth)
			{
				start--;
				placeBlock[avoidColumn] = false;
			}
			for (int i = start; i > drop; i--)
			{
				do {
					j = engine.random.nextInt(actualWidth);
				} while (!placeBlock[j]);
				placeBlock[j] = false;
			}
		}
		else
		{
			for (int x = 0; x < actualWidth; x++)
				placeBlock[x] = false;
			for (int i = 0; i < drop; i++)
			{
				do {
					j = engine.random.nextInt(actualWidth);
				} while (placeBlock[j] && j != avoidColumn);
				placeBlock[j] = true;
			}
		}

		for (int x = 0; x < actualWidth; x++)
			if (placeBlock[x])
				garbageDropPlace(x*bigMove, y, big, hard, color, countdown);
	}

	public boolean garbageDropPlace (int x, int y, boolean big, int hard)
	{
		return garbageDropPlace(x, y, big, hard, Block.BLOCK_COLOR_GRAY, 0);
	}
	public boolean garbageDropPlace (int x, int y, boolean big, int hard, int color)
	{
		return garbageDropPlace(x, y, big, hard, color, 0);
	}
	public boolean garbageDropPlace (int x, int y, boolean big, int hard, int color, int countdown)
	{
		Block b = getBlock(x, y);
		if (b == null)
			return false;
		if (big)
		{
			garbageDropPlace(x+1, y, false, hard);
			garbageDropPlace(x, y+1, false, hard);
			garbageDropPlace(x+1, y+1, false, hard);
		}
		if (getBlockEmptyF(x, y))
		{
			setBlockColor(x, y, color);
			b.setAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY, false);
			b.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
			b.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
			b.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
			b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
			b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
			b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
			b.hard = hard;
			b.secondaryColor = 0;
			b.countdown = countdown;
			return true;
		}
		return false;
	}

	public boolean canCascade() {
		for(int i = (getHeightWithoutHurryupFloor() - 1); i >= (hidden_height * -1); i--) {
			for(int j = 0; j < width; j++) {
				Block blk = getBlock(j, i);

				if((blk != null) && !blk.isEmpty() && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY)) {
					boolean fall = true;
					checkBlockLink(j, i);

					for(int k = (getHeightWithoutHurryupFloor() - 1); k >= (hidden_height * -1); k--) {
						for(int l = 0; l < width; l++) {
							Block bTemp = getBlock(l, k);

							if( (bTemp != null) && !bTemp.isEmpty() &&
								bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK) && !bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL) )
							{
								Block bBelow = getBlock(l, k + 1);

								if( (getCoordAttribute(l, k + 1) == COORD_WALL) ||
									((bBelow != null) && !bBelow.isEmpty() && !bBelow.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)) )
								{
									fall = false;
								}
							}
						}
					}

					if(fall)
						return true;
				}
			}
		}
		return false;
	}

	public void addRandomHoverBlocks(GameEngine engine, int count, int[] colors, int minY,
			boolean avoidLines)
	{
		addRandomHoverBlocks(engine, count, colors, minY, avoidLines, false);
	}

	public void addRandomHoverBlocks(GameEngine engine, int count, int[] colors, int minY,
			boolean avoidLines, boolean flashMode)
	{
		Random posRand = new Random(engine.random.nextLong());
		Random colorRand = new Random(engine.random.nextLong());
		int placeHeight = height-minY;
		int placeSize = placeHeight * width;
		boolean[][] placeBlock = new boolean[width][placeHeight];
		int[] colorCounts = new int[colors.length];
		for (int i = 0; i < colorCounts.length; i++)
			colorCounts[i] = 0;

		int blockColor;
		if (count < (placeSize >> 1))
		{
			int colorShift = colorRand.nextInt(colors.length);
			int x, y;
			for (y = 0; y < placeHeight; y++)
				for (x = 0; x < width; x++)
					placeBlock[x][y] = false;
			for (int i = 0; i < count; i++)
			{
				x = posRand.nextInt(width);
				y = posRand.nextInt(placeHeight);
				if (!getBlockEmpty(x, y+minY))
					i--;
				else
				{
					blockColor = ((i+colorShift)%colors.length);
					colorCounts[blockColor]++;
					addHoverBlock(x, y+minY, colors[blockColor]);
					placeBlock[x][y] = true;
				}
			}
		}
		else
		{
			int x, y;
			for (y = 0; y < placeHeight; y++)
				for (x = 0; x < width; x++)
					placeBlock[x][y] = true;
			for (int i = placeSize; i > count; i--)
			{
				x = posRand.nextInt(width);
				y = posRand.nextInt(placeHeight);
				if (placeBlock[x][y])
					placeBlock[x][y] = false;
				else
					i++;
			}
			for (y = 0; y < placeHeight; y++)
				for (x = 0; x < width; x++)
					if (placeBlock[x][y])
					{
						blockColor = colorRand.nextInt(colors.length);
						colorCounts[blockColor]++;
						addHoverBlock(x, y+minY, colors[blockColor]);
					}
		}
		if (!avoidLines || colors.length == 1)
			return;
		int colorUp, colorLeft, cIndex;
		for (int y = minY; y < height; y++)
			for (int x = 0; x < width; x++)
				if (placeBlock[x][y-minY])
				{
					colorUp = getBlockColor(x, y-2);
					colorLeft = getBlockColor(x-2, y);
					blockColor = getBlockColor(x, y);
					if (blockColor != colorUp && blockColor != colorLeft)
						continue;

					cIndex = -1;
					for (int i = 0; i < colorCounts.length; i++)
						if (colors[i] == blockColor)
						{
							cIndex = i;
							break;
						}

					if (colors.length == 2)
					{
						if ((colors[0] == colorUp && colors[1] != colorLeft) ||
								(colors[0] == colorLeft && colors[1] != colorUp))
						{
							colorCounts[1]++;
							colorCounts[cIndex]--;
							setBlockColor(x, y, colors[1]);
						}
						else if ((colors[1] == colorUp && colors[0] != colorLeft) ||
								(colors[1] == colorLeft && colors[0] != colorUp))
						{
							colorCounts[0]++;
							colorCounts[cIndex]--;
							setBlockColor(x, y, colors[0]);
						}
					}
					else
					{
						int newColor;
						do {
							newColor = colorRand.nextInt(colors.length);
						} while (colors[newColor] == colorUp || colors[newColor] == colorLeft);
						colorCounts[cIndex]--;
						colorCounts[newColor]++;
						setBlockColor(x, y, colors[newColor]);
					}
				}
		boolean[] canSwitch = new boolean[colors.length];
		int minCount = count/colors.length;
		int maxCount = (count+colors.length-1)/colors.length;
		boolean done = true;
		for (int i = 0; i < colorCounts.length; i++)
			if (colorCounts[i] > maxCount)
			{
				done = false;
				break;
			}
		int colorSide, bestSwitch, bestSwitchCount;
		int excess = 0;
		boolean fill = false;
		while (!done)
		{
			done = true;
			for (int y = minY; y < height; y++)
				for (int x = 0; x < width; x++)
				{
					blockColor = getBlockColor(x, y);
					fill = blockColor == Block.BLOCK_COLOR_NONE;
					cIndex = -1;
					if (!fill)
					{
						if (!placeBlock[x][y-minY])
							continue;
						for (int i = 0; i < colorCounts.length; i++)
							if (colors[i] == blockColor)
							{
								cIndex = i;
								break;
							}
						if (cIndex == -1)
							continue;
						if (colorCounts[cIndex] <= maxCount)
							continue;
					}
					for (int i = 0; i < colorCounts.length; i++)
						canSwitch[i] = colorCounts[i] < maxCount;

					colorSide = getBlockColor(x, y-2);
					for (int i = 0; i < colors.length; i++)
						if (colors[i] == colorSide)
						{
							canSwitch[i] = false;
							break;
						}
					colorSide = getBlockColor(x, y+2);
					for (int i = 0; i < colors.length; i++)
						if (colors[i] == colorSide)
						{
							canSwitch[i] = false;
							break;
						}
					colorSide = getBlockColor(x-2, y);
					for (int i = 0; i < colors.length; i++)
						if (colors[i] == colorSide)
						{
							canSwitch[i] = false;
							break;
						}
					colorSide = getBlockColor(x+2, y);
					for (int i = 0; i < colors.length; i++)
						if (colors[i] == colorSide)
						{
							canSwitch[i] = false;
							break;
						}
					bestSwitch = -1;
					bestSwitchCount = Integer.MAX_VALUE;
					for (int i = 0; i < colorCounts.length; i++)
						if (canSwitch[i] && colorCounts[i] < bestSwitchCount)
						{
							bestSwitch = i;
							bestSwitchCount = colorCounts[i];
						}
					if (bestSwitch != -1)
					{
						if (fill)
						{
							excess++;
							addHoverBlock(x, y, colors[bestSwitch]);
							placeBlock[x][y-minY] = true;
						}
						else
						{
							colorCounts[cIndex]--;
							setBlockColor(x, y, colors[bestSwitch]);
						}
						colorCounts[bestSwitch]++;
						done = false;
					}
				}
			while (excess > 0)
			{
				int x = posRand.nextInt(width);
				int y = posRand.nextInt(placeHeight)+minY;
				if (!placeBlock[x][y-minY])
					continue;
				blockColor = getBlockColor(x, y);
				for (int i = 0; i < colors.length; i++)
					if (colors[i] == blockColor)
					{
						if (colorCounts[i] > minCount)
						{
							setBlockColor(x, y, Block.BLOCK_COLOR_NONE);
							colorCounts[i]--;
							excess--;
							placeBlock[x][y-minY] = false;
						}
						break;
					}
			}
			boolean balanced = true;
			for (int i = 0; i < colorCounts.length; i++)
				if (colorCounts[i] > maxCount)
				{
					balanced = false;
					break;
				}
			if (balanced)
				done = true;
		}
		if (!flashMode)
			return;
		done = true;
		boolean[] gemNeeded = new boolean[colors.length];
		for (int i = 0; i < colors.length; i++)
		{
			if (colors[i] >= 2 && colors[i] <= 8 && colorCounts[i] > 0)
			{
				gemNeeded[i] = true;
				done = false;
			}
			else
				gemNeeded[i] = false;
		}
		while (!done)
		{
			int x = posRand.nextInt(width);
			int y = posRand.nextInt(placeHeight)+minY;
			if (!placeBlock[x][y-minY])
				continue;
			blockColor = getBlockColor(x, y);
			for (int i = 0; i < colors.length; i++)
				if (colors[i] == blockColor)
				{
					if (gemNeeded[i])
					{
						setBlockColor(x, y, blockColor+7);
						gemNeeded[i] = false;
					}
					break;
				}
			done = true;
			for (int i = 0; i < colors.length; i++)
				if (gemNeeded[i])
					done = false;
		}
	}
	public boolean addHoverBlock(int x, int y, int color)
	{
		Block b = getBlock(x, y);
		if (b == null)
			return false;
		b.color = color;
		b.setAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY, true);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
		return true;
	}

	public void shuffleColors(int[] blockColors, int numColors, Random rand) {
		blockColors = blockColors.clone();
		int maxX = Math.min(blockColors.length, numColors);
		int temp, j;
		int i = maxX;
		while (i > 1)
		{
			j = rand.nextInt(i);
			i--;
			if (j != i)
			{
				temp = blockColors[i];
				blockColors[i] = blockColors[j];
				blockColors[j] = temp;
			}
		}
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				temp = getBlockColor(x, y)-1;
				if (numColors == 3 && temp >= 3)
					temp--;
				if (temp >= 0 && temp < maxX)
					setBlockColor(x, y, blockColors[temp]);
			}
	}

	public int gemColorCheck(int size, boolean flag, boolean garbageClear, boolean ignoreHidden) {
		if (flag)
			setAllAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);

		Field temp = new Field(this);
		int total = 0;
		Block b;

		for(int i = (hidden_height * -1); i < getHeightWithoutHurryupFloor(); i++) {
			for(int j = 0; j < width; j++) {
				b = getBlock(j, i);
				if (b == null)
					continue;
				if (!b.isGemBlock())
					continue;
				int clear = temp.clearColor(j, i, false, garbageClear, true, ignoreHidden);
				if (clear >= size)
				{
					total += clear;
					if (flag)
						clearColor(j, i, true, garbageClear, true, ignoreHidden);
				}
			}
		}
		return total;
	}

	/**
	 * Instant avalanche, skips intermediate (cascade falling animation) steps.
	 * @return true if it affected the field at all, false otherwise.
	 */
	public boolean freeFall() {
		int y1, y2;
		boolean result = false;
		for (int x = 0; x < width; x++)
		{
			y1 = height-1;
			while (!getBlockEmpty(x, y1) && y1 >= (-1 * hidden_height))
				y1--;
			y2 = y1;
			while (getBlockEmpty(x, y2) && y2 >= (-1 * hidden_height))
				y2--;
			while (y2 >= (-1 * hidden_height))
			{
				setBlock(x, y1, getBlock(x, y2));
				setBlock(x, y2, new Block());
				y1--;
				y2--;
				result = true;
				while (getBlockEmpty(x, y2) && y2 >= (-1 * hidden_height))
					y2--;
			}
		}
		return result;
	}

	public void delEven() {
		for (int y = getHighestBlockY(); y < height; y++)
			if ((y&1) == 0)
				delLine(y);
	}

	public void delLower() {
		int rows = (height - getHighestBlockY() + 1) >> 1;
		for (int i = 1; i <= rows; i++)
			delLine(height-i);
	}

	public void delUpper() {
		int maxY = (height - getHighestBlockY()) >> 1;
		//TODO: Check if this should round up or down.
		for (int y = getHighestBlockY(); y <= maxY; y++)
			delLine(y);
	}

	public void delLine(int y) {
		for (int x = 0; x < width; x++)
		{
			Block b = getBlock(x, y);
			if (b != null)
				b.hard = 0;
		}
		setLineFlag(y, true);
	}

	public void moveLeft() {
		int x1, x2;
		for (int y = getHighestBlockY(); y < height; y++)
		{
			x1 = 0;
			while (!getBlockEmpty(x1, y))
				x1++;
			x2 = x1;
			while (x2 < width)
			{
				while (getBlockEmpty(x2, y) && x2 < width)
					x2++;
				setBlock(x1, y, getBlock(x2, y));
				setBlock(x2, y, new Block());
				x1++;
				x2++;
			}
		}
	}

	public void moveRight() {
		int x1, x2;
		for (int y = getHighestBlockY(); y < height; y++)
		{
			x1 = width-1;
			while (!getBlockEmpty(x1, y))
				x1--;
			x2 = x1;
			while (x2 >= 0)
			{
				while (getBlockEmpty(x2, y) && x2 >= 0)
					x2--;
				setBlock(x1, y, getBlock(x2, y));
				setBlock(x2, y, new Block());
				x1--;
				x2--;
			}
		}
	}

	public void negaField() {
		for (int y = getHighestBlockY(); y < height; y--)
			for (int x = 0; x < width; x++)
			{
				if (getBlockEmpty(x, y))
					garbageDropPlace(x, y, false, 0); //TODO: Set color
				else
					setBlockColor(x, y, Block.BLOCK_COLOR_NONE);
			}
	}

	public void flipVertical() {
		Block[] temp;
		for (int yMin = getHighestBlockY(), yMax = height-1; yMin < yMax; yMin--, yMax++)
		{
			if (yMin < 0)
			{
				temp = block_hidden[(yMin * -1) - 1];
				block_hidden[(yMin * -1) - 1] = block_field[yMax];
				block_field[yMax] = temp;
			}
			else
			{
				temp = block_field[yMin];
				block_field[yMin] = block_field[yMax];
				block_field[yMax] = temp;
			}
		}
	}

	public void mirror() {
		Block temp;

		for (int y = getHighestBlockY(); y < height; y--)
			for (int xMin = 0, xMax = width-1; xMin < xMax; xMin++, xMax--)
			{
				temp = getBlock(xMin, y);
				setBlock(xMin, y, getBlock(xMax, y));
				setBlock(xMax, y, temp);
			}
	}
}
