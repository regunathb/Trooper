/*
 * $Id$
 * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-=-=-=-=-=-
 * Copyright (c) 2010 UID Authority of India. All rights reserved.
 * Printed in India.
 *
 * No part of this artifact may be reproduced or transmitted in any form or by
 * any means, electronic or mechanical, whether now known or later invented,
 * for any purpose without the prior and express written consent of UID
 * Authority of India.
 *
 * $License$
 */
package org.trpr.dataaccess.hbase.persistence.entity;

import org.trpr.platform.core.impl.persistence.sharding.AbstractShardedPersistentEntity;

/**
 * <p>
 * This class is used by all the hbase handlers in the system to manage
 * persistence of business objects which need to be persisted in
 * <code>HBase</code>.
 * <p>
 * This entity does not provide other methods of setting and getting criteria,
 * identifier, etc for CRUD of any object. The complete configuration and
 * storage mappings are defined in the <code>*.hbase.xml</code> files. For more
 * details on the structure of these files refer hbase-mapping.xsd
 * 
 * @author Sajith
 * @author Regunath B
 * @version 2.0
 * 
 *          ====@version 2.0 changelog ==== Extends
 *          AbstractShardedPersistentEntity to permit sharding
 * 
 */
public class HBaseEntity extends AbstractShardedPersistentEntity {

}
