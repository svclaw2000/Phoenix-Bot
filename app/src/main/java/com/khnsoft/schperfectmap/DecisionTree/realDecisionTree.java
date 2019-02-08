package com.khnsoft.schperfectmap.DecisionTree;

import java.io.FileReader;
import java.util.Vector;


/** DecisionTree �� ���� interface ������ ����� Ŭ���� */
public class realDecisionTree 
{
	/** Decision tree�� �̸� */
	private String m_oName;
	
	/** �н��� ������ Ǯ ��� */
	private String m_oTrainingFilePath;
	/** �׽�Ʈ�� ������ Ǯ ��� */
	private String m_oTestFilePath;
	
	/** �н��� Item set */
	private ItemSet m_oTrainingSet;
	/** �׽�Ʈ�� Item set */
	private ItemSet m_oTestSet;
	
	/** Attribute Set ��ü(Training�� �����ͷκ��� ��´�) */
	private AttributeSet m_oTrainingAttributeSet;

	/** Test�� attribute ���� ��ü(�Ʒ��� �ִ� attribute set��ü�� ���ϱ� ���� �ӽð�ü) */
	private Vector m_oTestAttributesVector;
	/** Test�� attribute set ��ü */
	private AttributeSet m_oTestAttributeSet;
	
	/** Goal attribute */
	private SymbolicAttribute m_oGoalAttribute;
	
	/** training �������� ���� */
	private int m_nTotalTrainingItems;
	/** test �������� ���� */
	private int m_nTotalTestItems;
	
	/** test �����Ϳ��� "�� class����" prediction����, ���� �� ���� */
	private int [] m_anTotalCorrectPredictions;
	/** test �����Ϳ��� "�� class����" �� prediction ���� */
	private int [] m_anTotalPredictions;
	
	/** Decision Tree ��ü */
	private DecisionTree m_oDT;
	
	private Vector attributesRealTime;
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** ���� */
	public realDecisionTree( String oName, String oDotString ) throws Exception
	{		
		m_oName = oName;
		
		m_oTrainingFilePath = null;
		m_oTestFilePath = null;
		m_oTrainingSet = null;
		m_oTestSet = null;
		
		m_oTrainingAttributeSet = null;		
		m_oTestAttributesVector = null;
		m_oTestAttributeSet = null;
		
		m_oDT = new DecisionTree( oDotString );
	
		m_oDT.setGoalAttribute();
		m_oGoalAttribute = m_oDT.getGoalAttribute();
		
		m_nTotalTrainingItems = m_nTotalTestItems = 0;

		attributesRealTime = null;
	}
	
	
	/** ���� */
	public realDecisionTree( String oName, String oTrainingFile, String oTestFile ) throws Exception
	{		
		m_oName = oName;
		
		m_oTrainingFilePath = oTrainingFile;
		m_oTestFilePath = oTestFile;
		m_oTrainingSet = null;
		m_oTestSet = null;
		
		m_oTrainingAttributeSet = null;		
		m_oTestAttributesVector = null;
		m_oTestAttributeSet = null;
		
		m_oGoalAttribute = null;
		
		m_oDT = null;
		
		m_nTotalTrainingItems = m_nTotalTestItems = 0;
		attributesRealTime = null;
	}

	public IdSymbolicAttribute getGoalAttribute()
	{
		return (IdSymbolicAttribute)m_oGoalAttribute;
	}

	public void resetHash()
	{
		m_oDT.resetHash();
	}
	
	/** Decision tree�� parameter���� ���� ǥ�� */
	public void checkValidity()
	{
		AttributeSet oAS = m_oDT.getAttributeSet();
		System.out.println(  "" + oAS.size());
		int nCt, nCtMax = oAS.size();
		for( nCt = 0; nCt < nCtMax; ++nCt )
		{
			System.out.print( oAS.attribute( nCt ).name() + "\t" );
		}
		System.out.println();
	}
	
	/** �н���/�׽�Ʈ�� Item set ��ü ���ϰ�, Attribute set ��ü�� ���Ѵ�. */
	public void generateItemSet() throws Exception
	{
		m_oTrainingSet = ItemSetReader.read( new FileReader( m_oTrainingFilePath ) );
		
		if( m_oTrainingSet == null )
		{
			System.out.println( "generateItemSet() : Training item set is not generated." );
			return;
		}
		
		if( m_oTestFilePath != null && m_oTestFilePath.length() > 0 && m_oTestFilePath.compareTo( "none" ) != 0 )
		{
			m_oTestSet = ItemSetReader.read( new FileReader( m_oTestFilePath ), m_oTrainingSet.attributeSet() );
			m_nTotalTestItems = (int)m_oTestSet.size();
		}
		
		// Training�� Item set���� attribute set ��ü�� ����
		m_oTrainingAttributeSet = m_oTrainingSet.attributeSet();
		m_nTotalTrainingItems = (int)m_oTrainingSet.size();
	}

	public void setItemSet()
	{
		m_oTrainingAttributeSet = m_oDT.getAttributeSet();
	}
	
	/** Test�� �̿�� attribute���� training attribute���� index�� ���� �����ϰ�, Test�� attribute set ��ü�� ���Ѵ�.  */
	public void setTestAttributesByAttributeIndices( int [] anAttributeIndices )
	{
		m_oTestAttributesVector = new Vector();
		
		if( anAttributeIndices != null )
		{
			int nCt, nCtMax = anAttributeIndices.length;
			for( nCt = 0; nCt < nCtMax; ++nCt )
			{
				m_oTestAttributesVector.add( m_oTrainingAttributeSet.attribute( anAttributeIndices[ nCt ] ) );
			}
		}
		else
		{
			int nCt, nCtMax = m_oTrainingAttributeSet.size();
			for( nCt = 1; nCt < nCtMax; ++nCt )
			{
				m_oTestAttributesVector.add( m_oTrainingAttributeSet.attribute( nCt ) );
			}
		}
		
		m_oTestAttributeSet = new AttributeSet( m_oTestAttributesVector );
	}	

	public int getNumberOfAttributes() {
		return m_oTrainingAttributeSet.size();
	}
	
	/** Test�� �̿�� attribute���� "�̸�"���� �����ϰ�, Test�� attribute set ��ü�� ���Ѵ�.
	 	���⼭�� attribute "�̸�"����, training�� �����ͷ� ���� ���Ͽ� ��õǾ��ִ� ���� ����Ѵ�. */
	public void setTestAttributesByNames( String [] aoTestAttributeNames )
	{
		m_oTestAttributesVector = new Vector();
		
		int nCt, nCtMax = aoTestAttributeNames.length;
		for( nCt = 0; nCt < nCtMax; ++nCt )
		{
			m_oTestAttributesVector.add( m_oTrainingAttributeSet.findByName( aoTestAttributeNames[ nCt ] ) );
		}
		
		m_oTestAttributeSet = new AttributeSet( m_oTestAttributesVector );		
	}	

	public void setGoalAttribute()
	{
		m_oDT.setGoalAttribute();
	}
	
	/** ���ڷ� ������ attribute index�� ����Ͽ�, Goal attribute ���� */ 
	public void setGoalAttribute( int nAttributeIndex )
	{
		m_oGoalAttribute = (SymbolicAttribute)( m_oTrainingAttributeSet.attribute( nAttributeIndex ) );
	}
	
	/** attribute �̸��� ����Ͽ�, Goal attribute ���� */ 
	public void setGoalAttribute( String oAttributeName )
	{
		m_oGoalAttribute = (SymbolicAttribute)( m_oTrainingAttributeSet.findByName(oAttributeName) );
	}
	
    /** Decision tree ����.  (���ϵ� ����) */
    public DecisionTree generateDecisionTree()
    {
    	m_oDT = buildTree( m_oTrainingSet, m_oTestAttributeSet, m_oGoalAttribute );
    	
    	return m_oDT;
    }
    
    /** ������ �� ���� item( ��, decision tree�� attribute ��� ������ attribute���� ���� �� ���� item)�� ���ڷ� �޾Ƽ�,
     	"���� goal attribute �� ��"��  "���� goal attribute ��"�� ���Ͽ� ������ 1, �ٸ��� 0 ���� */
    public int [] TestWithArbitraryItem( Item oItem )
    {
    	int [] anRet = new int[ 2 ];
    	//AttributeSet itemAttributes = m_oDT.getAttributeSet();
    	
    	// ���ڷ� ���� item ����, "���� goal attribute ��"�� ����.
    	KnownSymbolicValue goalAttributeValue = (KnownSymbolicValue)oItem.valueOf( m_oTrainingAttributeSet, m_oGoalAttribute );
    	// Decision tree�� ����ؼ�, ���ڷ� ���� item�� ���� "���� goal attribute ��"�� ����.
    	KnownSymbolicValue guessedGoalAttributeValue = m_oDT.guessGoalAttribute( oItem );
    	
    	anRet[ 0 ] = goalAttributeValue.intValue;

    	if ( guessedGoalAttributeValue.equals( goalAttributeValue ) == true )
    	{
    		anRet[ 1 ] =  1;
    	}
    	else
    	{
    		anRet[ 1 ] =  0;
    	}
    	
    	return anRet;
    }
    
    /** training data file �� test data file �� ��θ� ���� */
	public void setTrainTestFile( String oTrainingFile, String oTestFile )
	{
		m_oTrainingFilePath = oTrainingFile;
		m_oTestFilePath = oTestFile;
	}
    
    /** Real-time data �̿��Ͽ� �׽�Ʈ �����ϰ�, prediction ��� ���� */
    public String TestWithRealTimeData(Vector<String> rawAttributes, Vector<Double> rawValues) throws Exception
    {
		if(attributesRealTime == null)
			attributesRealTime = ItemSetReader.readyToRealTimeRead(m_oTrainingAttributeSet);

		m_oTestSet = ItemSetReader.realTimeRead(
				attributesRealTime,
				rawAttributes,
				rawValues,
				m_oTrainingAttributeSet);
		
		// 맨 처음 한개만 적용
		Item testItem = m_oTestSet.item(0);

     	System.out.println( "#oTestItem.nbAttributes() = " + testItem.nbAttributes() );
    		 
    	KnownSymbolicValue guessedGoalAttributeValue = m_oDT.guessGoalAttribute( testItem );
//    	    KnownSymbolicValue goalAttributeValue = (KnownSymbolicValue)oTestItem.valueOf( m_oTrainingAttributeSet, m_oGoalAttribute );
    		    		 
    	int nRet = guessedGoalAttributeValue.intValue;
		return getGoalAttribute().itemOf(nRet);
    }
    
    /** Test data set�� �̿��Ͽ� �׽�Ʈ �����ϰ�, prediction accuracy ���� */
    public double [] TestWithTestData()
    {
    	int nCC, nCCmax = m_oGoalAttribute.nbValues;
    	
    	m_anTotalCorrectPredictions = new int [ nCCmax ];
    	m_anTotalPredictions = new int [ nCCmax ];
    	
    	for( nCC = 0; nCC < nCCmax; ++nCC )
		{
    		m_anTotalCorrectPredictions[ nCC ] = 0;
    		m_anTotalPredictions[ nCC ] = 0;
		}
    		
		for( int nCt = 0; nCt < m_nTotalTestItems; ++nCt )
		{
    		 Item oTestItem = m_oTestSet.item( nCt );      		 
    		 
    		 int [] anPredict = TestWithArbitraryItem( oTestItem ); 
    		 
    		 if( anPredict[ 1 ] == 1 )
			 {
				 m_anTotalCorrectPredictions[ anPredict[ 0 ] ] += 1;
			 }
    			 
   			 m_anTotalPredictions[ anPredict[ 0 ] ] += 1;    		 
		}    	
    	
    	return getPredictionAccuracy();
    }
    
    /** Training data set�� �̿��Ͽ� �׽�Ʈ �����ϰ�, prediction accuracy ���� */
    public double [] TestWithTrainingData()
    {
    	int nCC, nCCmax = m_oGoalAttribute.nbValues;
    	
    	m_anTotalCorrectPredictions = new int [ nCCmax ];
    	m_anTotalPredictions = new int [ nCCmax ];
    	
    	for( nCC = 0; nCC < nCCmax; ++nCC )
		{
    		m_anTotalCorrectPredictions[ nCC ] = 0;
    		m_anTotalPredictions[ nCC ] = 0;
		}
    		
		for( int nCt = 0; nCt < m_nTotalTrainingItems; ++nCt )
		{
    		 Item oTestItem = m_oTrainingSet.item( nCt );      		 
    		 
    		 int [] anPredict = TestWithArbitraryItem( oTestItem ); 
    		 
    		 if( anPredict[ 1 ] == 1 )
			 {
				 m_anTotalCorrectPredictions[ anPredict[ 0 ] ] += 1;
			 }
    			 
   			 m_anTotalPredictions[ anPredict[ 0 ] ] += 1;    		 
		}    	
    	
    	return getPredictionAccuracy();
    }
    
    /** prediction �� accuracy ��� ���� */
    public double [] getPredictionAccuracy()
    {
    	double [] alfAccuracies = new double[ m_anTotalPredictions.length ];
    	int nC, nCmax = alfAccuracies.length;
    	for( nC = 0; nC < nCmax; ++nC )
    	{
    		alfAccuracies[ nC ] = ( (double)m_anTotalCorrectPredictions[ nC ] ) / m_anTotalPredictions[ nC ];    	
    	}
    	
    	return alfAccuracies;    	
    }        
    	
    /** Returns a string that prints a dot file content depicting a tree. */
    public String getExpressionOfDT() 
    {
    	return ((new DecisionTreeToDot( m_oDT) ).jys_produce());
    }
    
    /** Decision Tree ���� */
    public String stateDecisionTree()
	{
		String oState = "[Decision Tree statement]\n";
		oState += ( "Training file path = " + m_oTrainingFilePath + "\tTest file path = " + m_oTestFilePath + "\n" );
		oState += ( "Goal attribute = " + this.m_oGoalAttribute.name() + "\n" );
		oState += ( "Open node exist? " + m_oDT.hasOpenNode() + "\n" );
		oState += ("Learning set size:" + m_oTrainingSet.size() + "\t# of attributes = " + m_oTrainingAttributeSet.size() + "\n" );
		
		oState += ( "Training attributes = {" );
		int nCt, nCtMax = m_oTrainingAttributeSet.size();
		for( nCt = 0; nCt < nCtMax; ++nCt )
		{
			oState += ( m_oTrainingAttributeSet.attribute( nCt ).name() + "," );
		}
		oState += "}\n";
		
		if( m_oTestSet != null )
		{
			oState += ("Test set size:" + m_oTestSet.size() + "\n" );
			oState += ( m_oTestAttributeSet.size() + " test attributes = {" );
			nCtMax = m_oTestAttributeSet.size();
			for( nCt = 0; nCt < nCtMax; ++nCt )
			{
				oState += ( m_oTestAttributeSet.attribute( nCt ).name() + "," );
			}		
			oState += "}\n";
		}
		else
		{
			oState += ("Test set size: 0\n" );			
		}		
		
		return oState;
	}
    
    /** decision tree ������ expression string���� �޾Ƽ� decision tree�� ���� ���Ͽ� �����ϴ� �Լ� */
	public DecisionTree loadDecisionTreeFromExpression( String oDotString )
	{
		return (new DecisionTree( oDotString ));
	}
	
	
	/** private �Լ��μ�, DecisionTree�� ���Ͽ� �����ϴ� �Լ� */
    private DecisionTree buildTree( ItemSet learningSet, AttributeSet testAttributes, SymbolicAttribute goalAttribute ) 
    {
		//SimpleDecisionTreeBuilder builder = 
		DecisionTreeBuilder builder =
				new DecisionTreeBuilder( learningSet, testAttributes, goalAttribute );
		
		//builder.setTestScoreThreshold( 0.0001 * learningSet.size() );
		
		return builder.build().decisionTree();
	}
    
    /** Remove items with a known value for the specified attribute. **/
    private void removeKnown( int attributeIndex, ItemSet set ) 
    {
		for( int i = 0; i < set.size(); i++ )
		{
		    if( !set.item( i ).valueOf( attributeIndex ).isUnknown() )
		    {
		    	set.remove(i--);
		    }
		}
    }
    
    
    /** Remove items with an unknown value for the specified attribute.  **/
    private void removeUnknown( int attributeIndex, ItemSet set ) 
    {
		for( int i = 0; i < set.size(); i++ )
		{
		    if( set.item(i).valueOf(attributeIndex).isUnknown() )
		    {
		    	set.remove(i--);
		    }
		}
    }	
    
}
