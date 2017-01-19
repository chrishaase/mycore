/*
 * 
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.common.events;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;

/**
 * Abstract helper class that can be subclassed to implement event handlers more
 * easily.
 * 
 * @author Frank Lützenkirchen
 * @author Jens Kupferschmidt
 */
public abstract class MCREventHandlerBase implements MCREventHandler {
    private static Logger logger = LogManager.getLogger(MCREventHandlerBase.class);

    /**
     * This method handle all calls for EventHandler for the event types
     * MCRObject, MCRDerivate and MCRFile.
     * 
     * @param evt
     *            The MCREvent object
     */
    public void doHandleEvent(MCREvent evt) {

        if (evt.getObjectType().equals(MCREvent.OBJECT_TYPE)) {
            MCRObject obj = (MCRObject) evt.get("object");
            if (obj != null) {
                logger.debug(getClass().getName() + " handling " + obj.getId().toString() + " " + evt.getEventType());
                if (evt.getEventType().equals(MCREvent.CREATE_EVENT)) {
                    handleObjectCreated(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.UPDATE_EVENT)) {
                    handleObjectUpdated(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    handleObjectDeleted(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.REPAIR_EVENT)) {
                    handleObjectRepaired(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.INDEX_EVENT)) {
                    handleObjectIndex(evt, obj);
                } else {
                    logger.warn("Can't find method for an object data handler for event type " + evt.getEventType());
                }
                return;
            }
            logger.warn("Can't find method for " + MCREvent.OBJECT_TYPE + " for event type " + evt.getEventType());
            return;
        }

        if (evt.getObjectType().equals(MCREvent.DERIVATE_TYPE)) {
            MCRDerivate der = (MCRDerivate) evt.get("derivate");
            if (der != null) {
                logger.debug(getClass().getName() + " handling " + der.getId().toString() + " " + evt.getEventType());
                if (evt.getEventType().equals(MCREvent.CREATE_EVENT)) {
                    handleDerivateCreated(evt, der);
                } else if (evt.getEventType().equals(MCREvent.UPDATE_EVENT)) {
                    handleDerivateUpdated(evt, der);
                } else if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    handleDerivateDeleted(evt, der);
                } else if (evt.getEventType().equals(MCREvent.REPAIR_EVENT)) {
                    handleDerivateRepaired(evt, der);
                } else if (evt.getEventType().equals(MCREvent.INDEX_EVENT)) {
                    updateDerivateFileIndex(evt, der);
                } else {
                    logger.warn("Can't find method for a derivate data handler for event type " + evt.getEventType());
                }
                return;
            }
            logger.warn("Can't find method for " + MCREvent.DERIVATE_TYPE + " for event type " + evt.getEventType());
            return;
        }

        if (evt.getObjectType().equals(MCREvent.PATH_TYPE)) {
            Path path = (Path) evt.get(MCREvent.PATH_KEY);
            if (path != null) {
                if (!path.isAbsolute()) {
                    logger.warn("Cannot handle path events on non absolute paths: " + path.toString());
                }
                logger.debug(getClass().getName() + " handling " + path.toString() + " " + evt.getEventType());
                BasicFileAttributes attrs = (BasicFileAttributes) evt.get(MCREvent.FILEATTR_KEY);
                if (attrs == null && !evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    logger.warn("BasicFileAttributes for " + path + " was not given. Resolving now.");
                    try {
                        attrs = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
                    } catch (IOException e) {
                        logger.error("Could not get BasicFileAttributes from path: " + path, e);
                    }
                }
                if (evt.getEventType().equals(MCREvent.CREATE_EVENT)) {
                    handlePathCreated(evt, path, attrs);
                } else if (evt.getEventType().equals(MCREvent.UPDATE_EVENT)) {
                    handlePathUpdated(evt, path, attrs);
                } else if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    handlePathDeleted(evt, path, attrs);
                } else if (evt.getEventType().equals(MCREvent.REPAIR_EVENT)) {
                    handlePathRepaired(evt, path, attrs);
                } else if (evt.getEventType().equals(MCREvent.INDEX_EVENT)) {
                    updatePathIndex(evt, path, attrs);
                } else {
                    logger.warn("Can't find method for Path data handler for event type " + evt.getEventType());
                }
                return;
            }
            logger.warn("Can't find method for " + MCREvent.PATH_TYPE + " for event type " + evt.getEventType());
            return;
        }

        if (evt.getObjectType().equals(MCREvent.CLASS_TYPE)) {
            MCRCategory cl = (MCRCategory) evt.get("class");
            if (cl != null) {
                logger.debug(getClass().getName() + " handling " + cl.getId() + " " + evt.getEventType());
                if (evt.getEventType().equals(MCREvent.CREATE_EVENT)) {
                    handleClassificationCreated(evt, cl);
                } else if (evt.getEventType().equals(MCREvent.UPDATE_EVENT)) {
                    handleClassificationUpdated(evt, cl);
                } else if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    handleClassificationDeleted(evt, cl);
                } else if (evt.getEventType().equals(MCREvent.REPAIR_EVENT)) {
                    handleClassificationRepaired(evt, cl);
                } else {
                    logger.warn("Can't find method for a classification data handler for event type "
                        + evt.getEventType());
                }
                return;
            }
            logger.warn("Can't find method for " + MCREvent.CLASS_TYPE + " for event type " + evt.getEventType());
            return;
        }

    }

    /**
     * This method roll back all calls for EventHandler for the event types
     * MCRObject, MCRDerivate and MCRFile.
     * 
     * @param evt
     *            The MCREvent object
     */
    public void undoHandleEvent(MCREvent evt) {

        if (evt.getObjectType().equals(MCREvent.OBJECT_TYPE)) {
            MCRObject obj = (MCRObject) evt.get("object");
            if (obj != null) {
                logger.debug(getClass().getName() + " handling " + obj.getId().toString() + " " + evt.getEventType());
                if (evt.getEventType().equals(MCREvent.CREATE_EVENT)) {
                    undoObjectCreated(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.UPDATE_EVENT)) {
                    undoObjectUpdated(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    undoObjectDeleted(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.REPAIR_EVENT)) {
                    undoObjectRepaired(evt, obj);
                } else {
                    logger.warn("Can't find method for an object data handler for event type " + evt.getEventType());
                }
                return;
            }
            logger.warn("Can't find method for " + MCREvent.OBJECT_TYPE + " for event type " + evt.getEventType());
            return;
        }

        if (evt.getObjectType().equals(MCREvent.DERIVATE_TYPE)) {
            MCRDerivate der = (MCRDerivate) evt.get("derivate");
            if (der != null) {
                logger.debug(getClass().getName() + " handling " + der.getId().toString() + evt.getEventType());
                if (evt.getEventType().equals(MCREvent.CREATE_EVENT)) {
                    undoDerivateCreated(evt, der);
                } else if (evt.getEventType().equals(MCREvent.UPDATE_EVENT)) {
                    undoDerivateUpdated(evt, der);
                } else if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    undoDerivateDeleted(evt, der);
                } else if (evt.getEventType().equals(MCREvent.REPAIR_EVENT)) {
                    undoDerivateRepaired(evt, der);
                } else {
                    logger.warn("Can't find method for a derivate data handler for event type " + evt.getEventType());
                }
                return;
            }
            logger.warn("Can't find method for " + MCREvent.DERIVATE_TYPE + " for event type " + evt.getEventType());
            return;
        }

        if (evt.getObjectType().equals(MCREvent.PATH_TYPE)) {
            Path path = (Path) evt.get(MCREvent.PATH_KEY);
            if (path != null) {
                if (!path.isAbsolute()) {
                    logger.warn("Cannot handle path events on non absolute paths: " + path.toString());
                }
                logger.debug(getClass().getName() + " handling " + path.toString() + " " + evt.getEventType());
                BasicFileAttributes attrs = (BasicFileAttributes) evt.get(MCREvent.FILEATTR_KEY);
                if (attrs == null && !evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    logger.warn("BasicFileAttributes for " + path + " was not given. Resolving now.");
                    try {
                        attrs = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
                    } catch (IOException e) {
                        logger.error("Could not get BasicFileAttributes from path: " + path, e);
                    }
                }
                if (evt.getEventType().equals(MCREvent.CREATE_EVENT)) {
                    undoPathCreated(evt, path, attrs);
                } else if (evt.getEventType().equals(MCREvent.UPDATE_EVENT)) {
                    undoPathUpdated(evt, path, attrs);
                } else if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    undoPathDeleted(evt, path, attrs);
                } else if (evt.getEventType().equals(MCREvent.REPAIR_EVENT)) {
                    undoPathRepaired(evt, path, attrs);
                } else {
                    logger.warn("Can't find method for Path data handler for event type " + evt.getEventType());
                }
                return;
            }
            logger.warn("Can't find method for " + MCREvent.PATH_TYPE + " for event type " + evt.getEventType());
            return;
        }

        if (evt.getObjectType().equals(MCREvent.CLASS_TYPE)) {
            MCRCategory obj = (MCRCategory) evt.get("class");
            if (obj != null) {
                logger.debug(getClass().getName() + " handling " + obj.getId() + " " + evt.getEventType());
                if (evt.getEventType().equals(MCREvent.CREATE_EVENT)) {
                    undoClassificationCreated(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.UPDATE_EVENT)) {
                    undoClassificationUpdated(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
                    undoClassificationDeleted(evt, obj);
                } else if (evt.getEventType().equals(MCREvent.REPAIR_EVENT)) {
                    undoClassificationRepaired(evt, obj);
                } else {
                    logger.warn("Can't find method for an classification data handler for event type "
                        + evt.getEventType());
                }
                return;
            }
            logger.warn("Can't find method for " + MCREvent.CLASS_TYPE + " for event type " + evt.getEventType());
            return;
        }

    }

    /** This method does nothing. It is very useful for debugging events. */
    public void doNothing(MCREvent evt, Object obj) {
        logger.debug(getClass().getName() + " does nothing on " + evt.getEventType() + " " + evt.getObjectType() + " "
            + obj.getClass().getName());
    }

    /**
     * Handles classification created events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRClassification that caused the event
     */
    protected void handleClassificationCreated(MCREvent evt, MCRCategory obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles classification updated events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRClassification that caused the event
     */
    protected void handleClassificationUpdated(MCREvent evt, MCRCategory obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles classification deleted events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRClassification that caused the event
     */
    protected void handleClassificationDeleted(MCREvent evt, MCRCategory obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles classification repair events. This implementation does nothing and should
     * be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRClassification that caused the event
     */
    protected void handleClassificationRepaired(MCREvent evt, MCRCategory obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles object created events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles object updated events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles object deleted events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected void handleObjectDeleted(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles object repair events. This implementation does nothing and should
     * be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    protected void handleObjectIndex(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles derivate created events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected void handleDerivateCreated(MCREvent evt, MCRDerivate der) {
        doNothing(evt, der);
    }

    /**
     * Handles derivate updated events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected void handleDerivateUpdated(MCREvent evt, MCRDerivate der) {
        doNothing(evt, der);
    }

    /**
     * Handles derivate deleted events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected void handleDerivateDeleted(MCREvent evt, MCRDerivate der) {
        doNothing(evt, der);
    }

    /**
     * Handles derivate repair events. This implementation does nothing and
     * should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected void handleDerivateRepaired(MCREvent evt, MCRDerivate der) {
        doNothing(evt, der);
    }

    protected void handlePathUpdated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    protected void handlePathDeleted(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    protected void handlePathRepaired(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    protected void updatePathIndex(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    protected void handlePathCreated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    /**
     * Handles undo of classification created events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRClassification that caused the event
     */
    protected void undoClassificationCreated(MCREvent evt, MCRCategory obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles undo of classification updated events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRClassification that caused the event
     */
    protected void undoClassificationUpdated(MCREvent evt, MCRCategory obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles undo of classification deleted events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRClassification that caused the event
     */
    protected void undoClassificationDeleted(MCREvent evt, MCRCategory obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles undo of classification repaired events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRClassification that caused the event
     */
    protected void undoClassificationRepaired(MCREvent evt, MCRCategory obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles undo of object created events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected void undoObjectCreated(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles undo of object updated events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected void undoObjectUpdated(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles undo of object deleted events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected void undoObjectDeleted(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles undo of object repaired events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param obj
     *            the MCRObject that caused the event
     */
    protected void undoObjectRepaired(MCREvent evt, MCRObject obj) {
        doNothing(evt, obj);
    }

    /**
     * Handles undo of derivate created events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected void undoDerivateCreated(MCREvent evt, MCRDerivate der) {
        doNothing(evt, der);
    }

    /**
     * Handles undo of derivate updated events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected void undoDerivateUpdated(MCREvent evt, MCRDerivate der) {
        doNothing(evt, der);
    }

    /**
     * Handles undo of derivate deleted events. This implementation does nothing
     * and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected void undoDerivateDeleted(MCREvent evt, MCRDerivate der) {
        doNothing(evt, der);
    }

    /**
     * Handles undo of derivate repaired events. This implementation does
     * nothing and should be overwritted by subclasses.
     * 
     * @param evt
     *            the event that occured
     * @param der
     *            the MCRDerivate that caused the event
     */
    protected void undoDerivateRepaired(MCREvent evt, MCRDerivate der) {
        doNothing(evt, der);
    }

    protected void undoPathCreated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    protected void undoPathUpdated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    protected void undoPathDeleted(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    protected void undoPathRepaired(MCREvent evt, Path path, BasicFileAttributes attrs) {
        doNothing(evt, path);
    }

    /**
     * Updates the index content of the given file.
     */
    protected void updateDerivateFileIndex(MCREvent evt, MCRDerivate file) {
        doNothing(evt, file);
    }
}
