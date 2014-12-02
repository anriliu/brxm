/*
 *  Copyright 2008-2014 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.hippoecm.frontend.plugins.development.content;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.wicket.util.io.IClusterable;
import org.hippoecm.frontend.plugins.development.content.names.Names;
import org.hippoecm.frontend.plugins.development.content.names.NamesFactory;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.NodeNameCodec;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowDescriptor;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.standardworkflow.EditableWorkflow;
import org.hippoecm.repository.standardworkflow.FolderWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentBuilder implements IClusterable {

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(ContentBuilder.class);

    private static final int DEFAULT_DEPTH = 3;
    private static final int DEFAULT_MIN_NR_OF_FOLDERS = 2;
    private static final int DEFAULT_MAX_NR_OF_FOLDERS = 2;

    private static final int DEFAULT_MIN_NAME_LENGTH = 10;
    private static final int DEFAULT_MAX_NAME_LENGTH = 35;

    private static final int DEFAULT_NUMBER_OF_DOCUMENTS = 5;

    private static final String DEFAULT_WORKFLOW_CATEGORY = "threepane";
    private static final String DEFAULT_NEW_DOCUMENT_CATEGORY = "new-document";

    private List<String> frequentlyUsedTags;
    private List<String> lessUsedTags;

    public static class NameSettings implements IClusterable {
        private static final long serialVersionUID = 1L;

        int minLength = DEFAULT_MIN_NAME_LENGTH;
        int maxLength = DEFAULT_MAX_NAME_LENGTH;

        public NameSettings() {
        }
    }

    public static class NodeTypeSettings implements IClusterable {
        private static final long serialVersionUID = 1L;

        //Collection<String> types = new LinkedList<>();
        Collection<CategoryType> types = new LinkedList<>();
        boolean random = true;

        public void setTypes(Collection<CategoryType> selectedTypes) {
            this.types = selectedTypes;
        }

        public Collection<CategoryType> getTypes() {
            return types;
        }

        public void setRandom(boolean random) {
            this.random = random;
        }

        public boolean isRandom() {
            return random;
        }
    }

    public static class NodeSettings implements IClusterable {
        private static final long serialVersionUID = 1L;

        String folderUUID;

        NameSettings naming;
        NodeTypeSettings nodeTypes;

        public NodeSettings() {
            naming = new NameSettings();
            nodeTypes = new NodeTypeSettings();
        }

        public NodeSettings(String folderUUID) {
            this();
            this.folderUUID = folderUUID;
        }
    }

    public static class FolderSettings extends NodeSettings {
        private static final long serialVersionUID = 1L;

        int depth = DEFAULT_DEPTH;
        int minimumChildNodes = DEFAULT_MIN_NR_OF_FOLDERS;
        int maximumChildNodes = DEFAULT_MAX_NR_OF_FOLDERS;

        DocumentSettings document = new DocumentSettings();

        public FolderSettings() {
            document.amount = 0;
        }

        public FolderSettings(String folderUUID) {
            super(folderUUID);
        }
    }

    public static class DocumentSettings extends NodeSettings {
        private static final long serialVersionUID = 1L;

        int amount = DEFAULT_NUMBER_OF_DOCUMENTS;
        boolean addTags = false;
        public int minNrOfTags = 5;
        public int maxNrOfTags = 15;

        public DocumentSettings() {
        }

        public DocumentSettings(String folderUUID) {
            super(folderUUID);
        }
    }


    String folderPath;
    //FolderWorkflow folderWorkflow;
    Names names;

    Collection<String> docTypes;
    Collection<String> folderTypes;

    private String workflowCategory;

    Random generator = new Random();

    public ContentBuilder() {
        this(DEFAULT_WORKFLOW_CATEGORY);
    }

    public ContentBuilder(String workflowCategory) {
        this.workflowCategory = workflowCategory;

        names = NamesFactory.newNames();
    }

    private String[] getTags(DocumentSettings settings) {
        createTags(settings.maxNrOfTags);

        int nrOfTags = getRandomAmount(settings.minNrOfTags, settings.maxNrOfTags);

        double amountOfTopTags = 0.75;
        Double result = nrOfTags * amountOfTopTags;

        int nrOfTopTags = result.intValue();
        int nrOfNormalTags = nrOfTags - nrOfTopTags;

        String[] topTags = getFrequentlyUsedTags(nrOfTopTags);
        String[] normalTags = getLessUsedTags(nrOfNormalTags);
        String[] tags = new String[nrOfTags];
        System.arraycopy(topTags, 0, tags, 0, topTags.length);
        System.arraycopy(normalTags, 0, tags, topTags.length, normalTags.length);
        Collections.shuffle(Arrays.asList(tags));
        return tags;
    }

    private void createTags(int maxNrOfTags) {
        if(frequentlyUsedTags == null) {
            List<String> tags = Arrays.asList(names.generate(maxNrOfTags * 5));
            frequentlyUsedTags = new ArrayList<>(maxNrOfTags);
            lessUsedTags = new ArrayList<>(maxNrOfTags * 4);
            for(int i=0; i<maxNrOfTags * 5; ++i) {
                if(i<maxNrOfTags) {
                    frequentlyUsedTags.add(tags.get(i));
                } else {
                    lessUsedTags.add(tags.get(i));
                }
            }
        }
    }

    private int getRandomAmount(int min, int max) {
        int amount = min;
        int diff = max - min;
        if (diff > 0) {
            amount += generator.nextInt(diff);
        }
        return amount;
    }

    private String[] getLessUsedTags(int nrOfTags) {
        return getTags(lessUsedTags,  nrOfTags);
    }

    private String[] getTags(List<String> data, int amount) {
        if(data == null) {
            throw new IllegalArgumentException("Data can't be null");
        }
        if(data.size() < amount) {
            throw new IllegalArgumentException("Requested number of tags exceeds cached values");
        }
        List<String> result = new ArrayList<>();
        while(result.size() < amount) {
            int index = generator.nextInt(amount);
            String tag = data.get(index);
            if(!result.contains(tag)) {
                result.add(tag);
            }
        }
        return result.toArray(new String[amount]);
    }

    private String[] getFrequentlyUsedTags(int nrOfTags) {
        return getTags(frequentlyUsedTags, nrOfTags);
    }

    interface NodeDecorator extends IClusterable {
        void decorate(Node node);    
    }

    @Deprecated
    private String createNode(String category, String[] typeAr, NameSettings settings, FolderWorkflow folderWorkflow, List<NodeDecorator> decorators) {
        int index = generator.nextInt(typeAr.length);
        String prototype = typeAr[index];
        String targetName = generateName(settings.minLength, settings.maxLength);
        String name = NodeNameCodec.encode(targetName, true);
        String path = null;
        try {
            path = folderWorkflow.add(category, prototype, name);
        } catch (RemoteException | RepositoryException | WorkflowException e) {
            log.error(String.format("Failed to create node of category='%s', prototype='%s' and name = '%s'", 
                    category, prototype, name), e);
        }

        if (path != null) {
            Session jcrSession = UserSession.get().getJcrSession();
            Node newNode = null;
            try {
                newNode = jcrSession.getRootNode().getNode(path.substring(1));
                WorkflowManager manager = ((HippoWorkspace) (jcrSession.getWorkspace())).getWorkflowManager();
                ((EditableWorkflow) manager.getWorkflow("default", newNode)).commitEditableInstance();

            } catch (RepositoryException | RemoteException | WorkflowException e) {
                log.error(String.format("Error creating new node of category='%s'", category), e);
            }

            if (newNode != null && decorators != null) {
                for (NodeDecorator decorator : decorators) {
                    decorator.decorate(newNode);
                }
                try {
                    jcrSession.save();
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
            return name;
        }
        return null;
    }    
    
    private String createNode(CategoryType[] cts, NameSettings settings, FolderWorkflow folderWorkflow, List<NodeDecorator> decorators) {
        int index = generator.nextInt(cts.length);
        final CategoryType ct = cts[index];

        String targetName = generateName(settings.minLength, settings.maxLength);
        String name = NodeNameCodec.encode(targetName, true);
        String path = null;
        try {
            path = folderWorkflow.add(ct.category, ct.type, name);
        } catch (RemoteException | RepositoryException | WorkflowException e) {
            log.error(String.format("Failed to create node of category='%s', type='%s' and name = '%s'", 
                    ct.category, ct.type, name), e);
        }

        if (path != null) {
            Session jcrSession = UserSession.get().getJcrSession();
            Node newNode = null;
            try {
                newNode = jcrSession.getRootNode().getNode(path.substring(1));
                WorkflowManager manager = ((HippoWorkspace) (jcrSession.getWorkspace())).getWorkflowManager();
                ((EditableWorkflow) manager.getWorkflow("default", newNode)).commitEditableInstance();

            } catch (RepositoryException | RemoteException | WorkflowException e) {
                log.error(String.format("Error creating new node of category='%s'", ct.category), e);
            }

            if (newNode != null && decorators != null) {
                for (NodeDecorator decorator : decorators) {
                    decorator.decorate(newNode);
                }
                try {
                    jcrSession.save();
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
            return name;
        }
        return null;
    }

    public void createDocuments(final DocumentSettings settings) {
        updateFolder(settings.folderUUID);

        if (settings.nodeTypes.random) {
            settings.nodeTypes.types = getDocumentTypes(settings.folderUUID);
        }

        List<NodeDecorator> decorators = new LinkedList<>();
        if(settings.addTags) {
            decorators.add(new NodeDecorator() {

                public void decorate(Node node) {
                    try {
                        if(node.isNodeType("hippostd:taggable")) {
                            node.setProperty("hippostd:tags", getTags(settings));
                        }
                    } catch (RepositoryException e) {
                        log.error("Error setting tag property", e);
                    }
                }
            });
        }

        FolderWorkflow folderWorkflow = getFolderWorkflow(settings.folderUUID);
        if (folderWorkflow != null) {
            CategoryType[] types = settings.nodeTypes.types.toArray(new CategoryType[settings.nodeTypes.types.size()]);

            for (int i = 0; i < settings.amount; i++) {
                createNode(types, settings.naming, folderWorkflow, decorators);
            }
        }
    }

    public void createFolders(FolderSettings settings, int depth) {
        createFolders(settings, depth, getFolderWorkflow(settings.folderUUID));
    }
    
    public void createFolders(FolderSettings settings, int depth, FolderWorkflow workflow) {
        updateFolder(settings.folderUUID);

        if (settings.nodeTypes.random) {
            settings.nodeTypes.types = getFolderTypes(settings.folderUUID);
        }

        if (settings.nodeTypes.types == null || settings.nodeTypes.types.size() == 0) {
            return;
        }

        if (workflow != null) {
            CategoryType[] types = settings.nodeTypes.types.toArray(new CategoryType[settings.nodeTypes.types.size()]);

            int amount = settings.minimumChildNodes;
            int diff = settings.maximumChildNodes - settings.minimumChildNodes;
            if (diff > 0) {
                amount += generator.nextInt(diff);
            }

            List<String> newNodes = new LinkedList<>();
            for (int i = 0; i < amount; i++) {
                newNodes.add(createNode(types, settings.naming, workflow, null));
            }

            String rootPath = folderPath;
            depth++;
            for (String newNode : newNodes) {
                settings.folderUUID = path2uuid(rootPath + "/" + newNode);

                if (settings.document.amount > 0) {
                    settings.document.folderUUID = settings.folderUUID;
                    createDocuments(settings.document);
                }

                if (depth <= settings.depth) {
                    createFolders(settings, depth);
                }
            }
        }
    }

    public List<CategoryType> getFolderTypes(String folderUUID) {
        return getCategoryTypes(folderUUID, "folder");
    }

    public List<CategoryType> getDocumentTypes(String folderUUID) {
        return getCategoryTypes(folderUUID, "document");
    }

    private List<CategoryType> getCategoryTypes(final String folderUUID, final String categoryName) {
        updateFolder(folderUUID);

        List<CategoryType> types = new LinkedList<>();
        for (final String category : getCategories(categoryName)) {
            for (final String type : getTypes(category, getFolderWorkflow(folderUUID))) {
                types.add(new CategoryType(category, type));
            }
        }
        return types;
    }

    private List<String> getCategories(final String hint) {
        List<String> categories = new LinkedList<>();
        Session session = UserSession.get().getJcrSession();
        try {
            Node node = session.getRootNode().getNode(folderPath.startsWith("/") ? folderPath.substring(1) : folderPath);
            for (Value v : node.getProperty("hippostd:foldertype").getValues()) {
                if (v.getString().contains(hint)) {
                    categories.add(v.getString());
                }
            }
        } catch ( RepositoryException e) {
            log.error("Failed to retrieve foldertype", e);
        }
        return categories;
    }

    @SuppressWarnings("unchecked")
    private List<String> getTypes(List<String> categories, FolderWorkflow folderWorkflow) {
        List<String> types = new LinkedList<>();
        if (folderWorkflow != null) {
            Map<String, Serializable> hints;
            try {
                hints = folderWorkflow.hints();
                final Map<String, Set<String>> prototypes = (Map<String, Set<String>>) hints.get("prototypes");
                for (String category : categories) {
                    if (prototypes.containsKey(category)) {
                        for (String s : prototypes.get(category)) {
                            types.add(s);
                        }
                    }
                }
            } catch (WorkflowException | RepositoryException | RemoteException e) {
                log.error("Failed to retrieve hints from folderWorkflow", e);
            }
        }
        return types;
    }

    @SuppressWarnings("unchecked")
    private List<String> getTypes(String category, FolderWorkflow folderWorkflow) {
        List<String> types = new LinkedList<>();
        if (folderWorkflow != null) {
            Map<String, Serializable> hints;
            try {
                hints = folderWorkflow.hints();
                final Map<String, Set<String>> prototypes = (Map<String, Set<String>>) hints.get("prototypes");
                if (prototypes.containsKey(category)) {
                    for (String s : prototypes.get(category)) {
                        types.add(s);
                    }
                }
            } catch (WorkflowException | RepositoryException | RemoteException e) {
                log.error("Failed to retrieve hints from folderWorkflow", e);
            }
        }
        return types;
    }

    public String generateName(int minLength, int maxLength) {

        StringBuilder name = new StringBuilder();
        int targetLength = generator.nextInt((maxLength + 1) - minLength) + minLength; 
        
        do {
            if (name.length() > 0) {
                name.append(" ");
            }
            names.setMaximumLength(targetLength - name.length());
            String newName = names.generate();
            if (newName == null) {
                break;
            }
            name.append(newName);
        } while (name.length() < targetLength);

        return name.toString();
    }

    private void updateFolder(String folderUUID) {
        String folder = uuid2path(folderUUID);
        if (folder != null && (folderPath == null || !folderPath.equals(folder))) {
            folderPath = folder;
        }
    }

    private FolderWorkflow getFolderWorkflow(String folderUUID) {
        updateFolder(folderUUID);
        Session jcrSession = UserSession.get().getJcrSession();

        try {
            final Node node = jcrSession.getRootNode().getNode(folderPath.startsWith("/") ? folderPath.substring(1) : folderPath);
            final WorkflowManager manager = ((HippoWorkspace) (jcrSession.getWorkspace())).getWorkflowManager();
            final WorkflowDescriptor folderWorkflowDescriptor = manager.getWorkflowDescriptor(workflowCategory, node);
            final Workflow workflow = (folderWorkflowDescriptor != null ? manager.getWorkflow(folderWorkflowDescriptor)
                    : null);
            if (workflow instanceof FolderWorkflow) {
                return (FolderWorkflow) workflow;
            }
        } catch (RepositoryException ex) {
            log.warn("Error while fetching FolderWorkflow: {}", ex.getMessage());
        }
        return null;
    }

    private String uuid2path(String uuid) {
        Session jcrSession = UserSession.get().getJcrSession();
        try {
            Node node = jcrSession.getNodeByIdentifier(uuid);
            return node.getPath();
        } catch (ItemNotFoundException e) {
            log.error("Node node found for uuid " + uuid, e);
        } catch (RepositoryException e) {
            log.error("Error while retrieving node path for uuid " + uuid, e);
        }
        return null;
    }

    private String path2uuid(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        //WorkflowManager manager = ((HippoWorkspace) (jcrSession.getWorkspace())).getWorkflowManager();
        //manager.getSession().save();
        Session jcrSession = UserSession.get().getJcrSession();
        try {
            if (jcrSession.getRootNode().hasNode(path)) {
                Node node = jcrSession.getRootNode().getNode(path);
                return node.getIdentifier();
            }
            log.error("Could not find node with path " + path);
        } catch (ItemNotFoundException e) {
            log.error("Node node found for path " + path, e);
        } catch (RepositoryException e) {
            log.error("Error while retrieving uuid for node path" + path, e);
        }
        return null;
    }
    
    public static class CategoryType implements IClusterable {
        String category;
        String type;

        public CategoryType(final String category, final String type) {
            this.category = category;
            this.type = type;
        }

        @Override
        public String toString() {
            return category + " - " + type;
        }
    }
}
