/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.utiltest;

import java.util.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.*;
import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.ZText;

/**
 * Unit test for ZSceneGraphEditor.
 * @author: Jesse Grosjean
 */
public class ZSceneGraphEditorTest extends TestCase {
    ZNode node = null;
    ZSceneGraphEditor editor = null;

    public ZSceneGraphEditorTest(String name) {
        super(name);
    }

    public void setUp() {
        node = new ZNode();
        editor = node.editor();
    }

    public void testCreationOrder() {
        assertEquals(editor.getAnchorGroup().getClass(), ZAnchorGroup.class);
        assertEquals(editor.getFadeGroup().getClass(), ZFadeGroup.class);
        assertEquals(editor.getInvisibleGroup().getClass(), ZInvisibleGroup.class);
        assertEquals(editor.getLayoutGroup().getClass(), ZLayoutGroup.class);
        assertEquals(editor.getNameGroup().getClass(), ZNameGroup.class);
        assertEquals(editor.getSelectionGroup().getClass(), ZSelectionGroup.class);
    //  assertEquals(editor.getSpatialIndexGroup().getClass(), ZSpatialIndexGroup.class); XXX Error
        assertEquals(editor.getStickyGroup().getClass(), ZStickyGroup.class);
        assertEquals(editor.getTransformGroup().getClass(), ZTransformGroup.class);

        ZNode parent = node.getParent();

    //  assertEquals(parent.getClass(), ZSpatialIndexGroup.class);
    //  parent = parent.getParent();*/

        assertEquals(parent.getClass(), ZFadeGroup.class);
        parent = parent.getParent();

        assertEquals(parent.getClass(), ZSelectionGroup.class);
        parent = parent.getParent();

        assertEquals(parent.getClass(), ZStickyGroup.class);
        parent = parent.getParent();

        assertEquals(parent.getClass(), ZTransformGroup.class);
        parent = parent.getParent();

        assertEquals(parent.getClass(), ZAnchorGroup.class);
        parent = parent.getParent();

        assertEquals(parent.getClass(), ZLayoutGroup.class);
        parent = parent.getParent();

        assertEquals(parent.getClass(), ZInvisibleGroup.class);
        parent = parent.getParent();

        assertEquals(parent.getClass(), ZNameGroup.class);
        parent = parent.getParent();

        assertNull(parent);
    }

    public void testHasOneChildBug() {
        ZGroup group = new ZGroup();
        group.setHasOneChild(true);
        group.editor();
    }

    public void testGetNode() {
        ZAnchorGroup agroup = editor.getAnchorGroup();
        ZFadeGroup fgroup = editor.getFadeGroup();

        assertEquals(fgroup.editor().getNode(), node);
        assertEquals(agroup.editor().getNode(), node);
    }

    public void testGetTop() {
        editor.getAnchorGroup();
        editor.getFadeGroup();

        assertNull(editor.getTop().getParent());
        new ZGroup().addChild(editor.getTop());
        assertNotNull(editor.getTop().getParent());
    }

    public void testHasGroup() {
        ZAnchorGroup agroup = editor.getAnchorGroup();
        ZFadeGroup fgroup = editor.getFadeGroup();

        assertTrue(editor.hasAnchorGroup());
        assertTrue(editor.hasFadeGroup());
        assertTrue(!editor.hasTransformGroup());
        editor.removeAnchorGroup();
        assertTrue(!editor.hasAnchorGroup());
    }

    public void testRemove() {
        ZAnchorGroup agroup = editor.getAnchorGroup();
        ZFadeGroup fgroup = editor.getFadeGroup();

        assertTrue(editor.hasAnchorGroup());
        assertTrue(editor.hasFadeGroup());

        editor.removeAnchorGroup();
        editor.removeFadeGroup();

        assertTrue(!editor.hasAnchorGroup());
        assertTrue(!editor.hasFadeGroup());
    }

    public void testCustomEditGroup() {
        ZNode node = new ZNode();
        ZVisualGroup eg = (ZVisualGroup) node.editor().getEditGroup(ZVisualGroup.class);
        ZGroup g = node.editor().getEditGroup(ZGroup.class);
        node.editor().hasEditGroup(ZVisualGroup.class);
        node.editor().hasEditGroup(ZVisualGroup.class);
        node.editor().getAnchorGroup();
        node.editor().getFadeGroup();
        node.editor().getTransformGroup();
        assertTrue(node.editor().hasTransformGroup());
        assertTrue(node.editor().hasAnchorGroup());
        assertTrue(node.editor().hasFadeGroup());

        assertEquals(node.editor().getEditGroup(ZVisualGroup.class), eg);
        assertEquals(node.editor().getEditGroup(ZGroup.class), g);
    }

    public void testNewEditor() {
        ZNode n = new ZNode();
        ZSceneGraphEditor newEditor = new ZSceneGraphEditor(n);

        ZTransformGroup g = newEditor.getTransformGroup();
        assertTrue(newEditor.hasTransformGroup());
        newEditor.removeTransformGroup();
        assertTrue(!newEditor.hasTransformGroup());

        g = newEditor.getTransformGroup();
        assertEquals(g, (new ZSceneGraphEditor(newEditor.getTransformGroup())).getTransformGroup());
        assertEquals(n, newEditor.getNode());
        assertEquals(n, (new ZSceneGraphEditor(newEditor.getTransformGroup())).getNode());

        assertEquals(newEditor.getTop(), g);
        assertEquals(newEditor.getTop(), g);

        ZFadeGroup fg = newEditor.getFadeGroup();
        assertTrue(newEditor.hasTransformGroup());
        assertTrue(newEditor.hasFadeGroup());
        newEditor.removeFadeGroup();
        assertTrue(newEditor.hasTransformGroup());
        assertTrue(!newEditor.hasFadeGroup());
        assertTrue(newEditor.getFadeGroup() != fg);

    }
}