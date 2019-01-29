/* jaDTi package - v0.6.1 */

/*
 *  Copyright (c) 2004, Jean-Marc Francois.
 *
 *  This file is part of jaDTi.
 *  jaDTi is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  jaDTi is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jahmm; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */

package com.khnsoft.schperfectmap.DecisionTree;

import java.util.*;

// import Utils.// OutputManager;


/**
 * This class implements a decision tree.<p>
 * A decision tree is a tree where a test has been assigned to non-leaf
 * nodes.  Its aim is to guess the value of an {@link Item item}'s attribute
 * (called the 'goal' attribute) thanks to tests over other attributes.<p>
 * If the topology of the tree is changed, take must be taken to create a
 * valid tree, i.e. an acyclic graph where all the sons of one node are
 * different.<p>
 * The tree is composed of 3 types of nodes:
 * <ul>
 * <li> TestNodes: They are associated to a test over items' attributes.  A
 *      test node has as many sons as the test's number of different outcomes.
 * <li> LeafNodes: They have no sons.  A leaf node is associated a goal
 *      attribute value.
 * <li> OpenNode: A node whose purpose has not been found yet.  It can be
 *      replaced by a test/leaf node later on.  The tree's open nodes can be
 *      efficiently (in log(nbNodes) on average) retreived.
 * </ul>
 **/
public class DecisionTree {

	private final AnchorNode anchor;
	private AttributeSet attributeSet;
	private SymbolicAttribute goalAttribute;


	/**
	 * Creates a empty decision tree.
	 *
	 * @param attributeSet A set of attribute.  The set of attributes of the
	 *                     items given to this tree.  Can be set to 'null'.
	 * @param goalAttribute The goalAttribute.  Can be set to 'null'.
	 **/
	public DecisionTree(AttributeSet attributeSet, 
			SymbolicAttribute goalAttribute) 
	{
		anchor = new AnchorNode(this);
		this.attributeSet = attributeSet;
		this.goalAttribute = goalAttribute;
	}
	
	/** attributes �� ���� expression ���� */
	public String getExpressionOfAttributes()
	{		
		return attributeSet.getExpression();
	}
	
	/** expression �� ����, attribute set �� ���� */
	public void setAttributeSetByExpression( String oExp )
	{
		attributeSet = new AttributeSet( null );
		String [] aoSplit = oExp.split( "\n" );
		int nCt, nCtMax = aoSplit.length;
		for( nCt = 1; nCt < nCtMax; ++nCt )
		{
			String [] aoSplit2 = aoSplit[ nCt ].split( "\t" );
			int nType = Integer.valueOf( aoSplit2[ 0 ] ).intValue();
			
			Attribute oNewAtt = null;
			
			// IdSymbolicAttribute
			if( nType == 0 )
			{
				oNewAtt = new IdSymbolicAttribute( aoSplit[ nCt ] );
			}
			// NumericalAttribute
			else if( nType == 1 )
			{
				oNewAtt = new NumericalAttribute( aoSplit[ nCt ], null );				
			}
			// IdSymbolicAttribute
			else
			{
				oNewAtt = new SymbolicAttribute( aoSplit[ nCt ] );
			}
			
			attributeSet.add( oNewAtt );
			
		}	
		goalAttribute = (SymbolicAttribute)attributeSet.attribute( attributeSet.size() - 1 );
	}
	

	/** private �Լ��μ�, Dot �������� ǥ���� tree string���κ���, attribute set�� �����Ͽ� ���� */
	private AttributeSet loadAttributeSet( String oDotTree )
	{
		//AttributeSet oRet = new AttributeSet( null );
		AttributeSet oRet = this.attributeSet;

		String oLine = "";
		String [] aoSplit = oDotTree.split( "\n" );
		int nCt, nCtMax = aoSplit.length;

		String oLeafSymbolName = null;
		Vector<String> oLeafSymbolVoca = null;

		// �� ���ٰ� �� �Ʒ����� skip
		for( nCt = 1; nCt < nCtMax - 1; ++nCt )
		{
			oLine = aoSplit[ nCt ].trim();

			int nTemp = oLine.substring( 1 ).indexOf( "\"" );
			int nTemp2 = oLine.substring( 1 ).indexOf( "@" );
			int nTemp3 = oLine.substring( 1 ).indexOf( "." );

			String oNodeName = oLine.substring( nTemp2 + 2, nTemp + 1 );
			String oNodeType = oLine.substring( nTemp3 + 2, nTemp2 + 1 );

			// tree������ edge ������ ���,
			if( oLine.contains( "->" ) == true )
			{
				// �ƹ��͵� �� �Ѵ�.
			}
			// tree�� node ������ ���,
			else
			{
				String oTempLine = oLine.substring( nTemp + 11 );

				// Leaf node�� ���,
				if( oNodeType.compareTo( "LeafNode" ) == 0 )
				{
					nTemp = oTempLine.indexOf( "[" );
					nTemp2 = oTempLine.indexOf( "]" );        
					String oSymbols = oTempLine.substring( nTemp + 1, nTemp2 ).trim();

					// symbol ���� ����
					String [] aoSyms = oSymbols.split( " " );
					int [] anSyms = new int[ aoSyms.length ];
					int nTotalSym = aoSyms.length;
					if( oLeafSymbolVoca == null )
					{
						oLeafSymbolVoca = new Vector<String>();
						for( int nC = 0; nC < nTotalSym; ++nC )
						{
							oLeafSymbolVoca.add( " " );        					
						}        				
					}
					else
					{
						if( nTotalSym != oLeafSymbolVoca.size() )
						{
							// OutputManager.Error( "loadAttributeSet() : Something wrong!(inconsistent # of symbols)" );
							return null;
						}
					}

					// attribute "�̸�" ����
					oLeafSymbolName = oTempLine.substring( 0, nTemp );

					// attribute "value" ����
					int nTemp4 = oTempLine.indexOf( "-" );
					int nTemp5 = oTempLine.substring( nTemp4 + 2 ).indexOf( " " );
					String oAttValue = oTempLine.substring( nTemp4 + 2  ).substring( 0, nTemp5 );
					oAttValue = oAttValue.trim();
					for( int nC = 0; nC < oLeafSymbolVoca.size(); ++nC )
					{
						if( aoSyms[ nC ].compareTo( "1" ) == 0 )
						{
							anSyms[ nC ] = 1;   
							oLeafSymbolVoca.set( nC, oAttValue );
						}
						else
						{
							anSyms[ nC ] = 0;
						}        				
					}    

				}
				// ScoreTest node�� ���,
				// (score, weight, test)�� �����Ͽ� ScoreTestNode�� �����ؾ� �Ѵ�.
				else
				{
					// numerical attribute������ üũ
					nTemp3 = oTempLine.indexOf( "<" );
					if( nTemp3 < 0 )
					{
						nTemp3 = oTempLine.indexOf( ">" );        			      			       			
					}

					// Numerical attribute�� �ƴ� ���,
					if( nTemp3 < 0 )
					{
						nTemp = oTempLine.indexOf( "[" );
						nTemp2 = oTempLine.indexOf( "]" );        
						String oSymbols = oTempLine.substring( nTemp + 1, nTemp2 ).trim();

						// symbol�� ���� �� �� ���,
						if( oSymbols.contains( " "  ) == true )
						{
							// �ϴ�, ������.
						}
						// symbol�� boolean (yes/no)�� ���,
						else
						{
							// symbol ���� ����
							int nTotalSym = 1;

							// attribute �̸� ����
							int nTemp4 = oTempLine.indexOf( " " );
							String oAttName = oTempLine.substring( 0, nTemp4 + 1 );
							oAttName = oAttName.trim();

							// symbol �迭 ����
							KnownSymbolicValue [] values = new KnownSymbolicValue[ nTotalSym ];
							values[ 0 ] = new KnownSymbolicValue( 0 );

							// ����� symbol��, attribute�� "ù ��°" ���� label�̹Ƿ�,
							// yes/no ������ ���� ������ �����Ѵ�.
							String oS1, oS2;
							if( oSymbols.compareTo( "yes" ) == 0 )
							{
								oS1 = "yes";            
								oS2 = "no";
							}
							else
							{
								oS1 = "no";            
								oS2 = "yes";                				
							}
							Vector<String> oAttributeVocas = new Vector<String>();
							oAttributeVocas.add( oS1 );
							oAttributeVocas.add( oS2 );

							// Attribute  ����
							IdSymbolicAttribute oIDSA = new IdSymbolicAttribute( oAttName, oAttributeVocas );
							// ������ attribute�� ���ݲ� ����� �͵���� �ٸ� unique�� ���̸�, attribute set�� �߰���Ŵ.
							
							int nIdx = oRet.indexOfName( oAttName );
							if( nIdx < 0 )
							{
								// OutputManager.Error( "loadAttributeSet() : What a fuck." );
//								oRet.add( oIDSA );
								return null;
							}            			
							else
							{
								oRet.replaceAttribute( oIDSA, nIdx );
							}

						}


					}
					// Numerical attribute �� ���,
					else
					{
						nTemp = oTempLine.indexOf( "<" );
						String oAttName = oTempLine.substring( 0, nTemp - 1 ).trim();

						// Attribute  ����
						NumericalAttribute oNA = new NumericalAttribute( oAttName );
						// ������ attribute�� ���ݲ� ����� �͵���� �ٸ� unique�� ���̸�, attribute set�� �߰���Ŵ.
						if( oRet.findByName( oAttName ) == null )
						{
							oRet.add( oNA );            				
						}   
					}

				}


			}    		

		}    	

		// Leaf ����� Attribute  ���� (��, goal attribute)
		IdSymbolicAttribute oIDSA = new IdSymbolicAttribute( oLeafSymbolName, oLeafSymbolVoca );
		// ������ attribute�� ���ݲ� ����� �͵���� �ٸ� unique�� ���̸�, attribute set�� �߰���Ŵ.
		if( oRet.findByName( oLeafSymbolName ) == null )
		{
			oRet.add( oIDSA );            				
		}        
		goalAttribute = oIDSA;

		return oRet;
	}
	
	/** Dot �������� ǥ���� tree string���κ���, ScoreTestNode���� ���͸� �����Ͽ� ����.
	 	Ư��, root node�� ScoreTestNode�� anchor �ٷ� �ؿ� �߰������ش�.  */
	private Vector<ScoreTestNode> loadScoreTestNodeSet( String oDotTree )
	{
		Vector<ScoreTestNode> oRet = new Vector<ScoreTestNode>();

		String oLine = "";
		String [] aoSplit = oDotTree.split( "\n" );
		int nCt, nCtMax = aoSplit.length;

		// �� ���ٰ� �� �Ʒ����� skip
		for( nCt = 1; nCt < nCtMax - 1; ++nCt )
		{
			oLine = aoSplit[ nCt ].trim();

			int nTemp = oLine.substring( 1 ).indexOf( "\"" );
			int nTemp2 = oLine.substring( 1 ).indexOf( "@" );
			int nTemp3 = oLine.substring( 1 ).indexOf( "." );

			String oNodeName = oLine.substring( nTemp2 + 2, nTemp + 1 );
			String oNodeType = oLine.substring( nTemp3 + 2, nTemp2 + 1 );

			// tree������ edge ������ ���,
			if( oLine.contains( "->" ) == true )
			{
				// �ƹ��͵� ����.
			}
			// tree�� node ������ ���,
			else
			{
				String oTempLine = oLine.substring( nTemp + 11 );   	

				// node�� "Weight" ���� ����.
				int nWeightIdx1 = oTempLine.indexOf( "Weight = " );
				int nWeightIdx2 = oTempLine.substring( nWeightIdx1 + 9 ).indexOf( "\"" );
				double lfNodeWeight = Integer.valueOf( oTempLine.substring( nWeightIdx1 + 9, nWeightIdx1 + 9 + nWeightIdx2 ) );

				// Leaf node�� ���,
				// (weight)�� �ʿ��ϴ�.
				if( oNodeType.compareTo( "LeafNode" ) == 0 )
				{
					// �ƹ��͵� �� ��.
				}
				// ScoreTest node�� ���,
				// (score, weight, test)�� �����Ͽ� ScoreTestNode�� �����ؾ� �Ѵ�.
				else
				{
					// node�� "score" ���� ����.
					int nScoreIdx1 = oTempLine.indexOf( "score= " );
					int nScoreIdx2 = oTempLine.substring( nScoreIdx1 + 7 ).indexOf( ")" );        			
					double lfNodeScore = Double.valueOf( oTempLine.substring( nScoreIdx1 + 7, nScoreIdx1 + 7 + nScoreIdx2 ) );

					// numerical attribute������ üũ
					nTemp3 = oTempLine.indexOf( "<" );
					if( nTemp3 < 0 )
					{
						nTemp3 = oTempLine.indexOf( ">" );        			      			       			
					}

					// Numerical attribute�� �ƴ� ���,
					if( nTemp3 < 0 )
					{
						nTemp = oTempLine.indexOf( "[" );
						nTemp2 = oTempLine.indexOf( "]" );        
						String oSymbols = oTempLine.substring( nTemp + 1, nTemp2 ).trim();

						// symbol�� ���� �� �� ���,
						if( oSymbols.contains( " "  ) == true )
						{
							// ó�� ����. ����.
						}
						// symbol�� boolean (yes/no)�� ���,
						else
						{
							// symbol ���� ����
							int nTotalSym = 1;

							// attribute �̸� ����
							int nTemp4 = oTempLine.indexOf( " " );
							String oAttName = oTempLine.substring( 0, nTemp4 + 1 );
							oAttName = oAttName.trim();

							// �ش� attribute ����
							IdSymbolicAttribute oIDSA = (IdSymbolicAttribute)attributeSet.findByName( oAttName );

							// symbol �迭 ����
							KnownSymbolicValue [] values = new KnownSymbolicValue[ nTotalSym ];
							values[ 0 ] = new KnownSymbolicValue( 0 );

							// ScoreTestNode ����!
							ScoreTestNode oSTN = new ScoreTestNode( lfNodeWeight, new SymbolicTest( oIDSA, values ), lfNodeScore );
							oSTN.m_oNickName = oNodeName;
							oSTN.setHasOpenNode( false );

							// root ����� ����� ���, anchor �ؿ� �߰�
							if( nCt == 1 )
							{
								oSTN.setFather( anchor );
								anchor.replaceSon( anchor.son(), oSTN );
							}
							
							oRet.add( oSTN );
						}
						
					}
					// Numerical attribute �� ���,
					else
					{
						int nTemp4 = oTempLine.indexOf( "<" );
						String oAttName = oTempLine.substring( 0, nTemp4 - 1 ).trim();
						int nTemp5 = oTempLine.substring( nTemp4 + 2 ).indexOf( " " );            			
						String oAttValue = oTempLine.substring( nTemp4 + 1, nTemp4 + nTemp5 + 2 ).trim();
						double lfAttValue = Double.valueOf( oAttValue );

						// Attribute  ���
						NumericalAttribute oNA = (NumericalAttribute)attributeSet.findByName( oAttName );

						// ScoreTestNode ����!
						ScoreTestNode oSTN = new ScoreTestNode( lfNodeWeight, new NumericalTest( oNA, lfAttValue ), lfNodeScore );
						oSTN.m_oNickName = oNodeName;
						oSTN.setHasOpenNode( false );

						// root ����� ����� ���, anchor �ؿ� �߰�
						if( nCt == 1 )
						{
							oSTN.setFather( anchor );
							anchor.replaceSon( anchor.son(), oSTN );
						}
						
						oRet.add( oSTN );
					}

				}

			}    // tree �� edge�� ���� node�� ��츦 ��� ����ߴ� if��.

		}  // ��ü for��.    	
		
		return oRet;
		
	}
	
	/** Dot �������� ǥ���� tree string���κ���, LeafNode���� ���͸� �����Ͽ� ����. */
private Vector<LeafNode> loadLeafNodeSet( String oDotTree )
{
	Vector<LeafNode> oRet = new Vector<LeafNode>();

	String oLine = "";
	String [] aoSplit = oDotTree.split( "\n" );
	int nCt, nCtMax = aoSplit.length;

	// �� ���ٰ� �� �Ʒ����� skip
	for( nCt = 1; nCt < nCtMax - 1; ++nCt )
	{
		oLine = aoSplit[ nCt ].trim();

		int nTemp = oLine.substring( 1 ).indexOf( "\"" );
		int nTemp2 = oLine.substring( 1 ).indexOf( "@" );
		int nTemp3 = oLine.substring( 1 ).indexOf( "." );

		String oNodeName = oLine.substring( nTemp2 + 2, nTemp + 1 );
		String oNodeType = oLine.substring( nTemp3 + 2, nTemp2 + 1 );

		// tree������ edge ������ ���,
		if( oLine.contains( "->" ) == true )
		{
			// �ƹ��͵� ����.
		}
		// tree�� node ������ ���,
		else
		{
			String oTempLine = oLine.substring( nTemp + 11 );   	

			// node�� "Weight" ���� ����.
			int nWeightIdx1 = oTempLine.indexOf( "Weight = " );
			int nWeightIdx2 = oTempLine.substring( nWeightIdx1 + 9 ).indexOf( "\"" );
			double lfNodeWeight = Integer.valueOf( oTempLine.substring( nWeightIdx1 + 9, nWeightIdx1 + 9 + nWeightIdx2 ) );

			// Leaf node�� ���,
			// (weight)�� �ʿ��ϴ�.
			if( oNodeType.compareTo( "LeafNode" ) == 0 )
			{
				nTemp = oTempLine.indexOf( "[" );
				nTemp2 = oTempLine.indexOf( "]" );        
				String oSymbols = oTempLine.substring( nTemp + 1, nTemp2 ).trim();

				// symbol ���� ����
				String [] aoSyms = oSymbols.split( " " );
				double [] alfSyms = new double[ aoSyms.length ];
				int nTotalSym = aoSyms.length;
				
				// attribute "�̸�" ����
				String oLeafSymbolName = oTempLine.substring( 0, nTemp );

				// attribute "value" ����
				int nTemp4 = oTempLine.indexOf( "-" );
				int nTemp5 = oTempLine.substring( nTemp4 + 2 ).indexOf( " " );
				String oAttValue = oTempLine.substring( nTemp4 + 2  ).substring( 0, nTemp5 );
				oAttValue = oAttValue.trim();
				for( int nC = 0; nC < nTotalSym; ++nC )
				{
					if( aoSyms[ nC ].compareTo( "1" ) == 0 )
					{
						alfSyms[ nC ] = 1;   
						
					}
					else
					{
						alfSyms[ nC ] = 0;
					}        				
				}    
				
				LeafNode oNewLeafNode = new LeafNode( lfNodeWeight );
				oNewLeafNode.m_oNickName = oNodeName;
				oNewLeafNode.setGoalValueDistribution( alfSyms );
				
				oRet.add( oNewLeafNode );
			}		

		}    // tree �� edge�� ���� node�� ��츦 ��� ����ߴ� if��.

	}  // ��ü for��.    	
	
	return oRet;
	
}
	
	/** Dot �������� ǥ���� tree string���κ���, edge���� load�Ͽ�  tree ���� */
	private void loadEdgeSet( String oDotTree, Vector<ScoreTestNode> oScoreTestNodeSet, Vector<LeafNode> oLeafNodeSet )
	{
		String oLine = "";
		String [] aoSplit = oDotTree.split( "\n" );
		int nCt, nCtMax = aoSplit.length;

		// �� ���ٰ� �� �Ʒ����� skip
		for( nCt = 1; nCt < nCtMax - 1; ++nCt )
		{
			oLine = aoSplit[ nCt ].trim();

			int nTemp = oLine.substring( 1 ).indexOf( "\"" );
			int nTemp2 = oLine.substring( 1 ).indexOf( "@" );
			int nTemp3 = oLine.substring( 1 ).indexOf( "." );

			String oNodeName = oLine.substring( nTemp2 + 2, nTemp + 1 ).trim();
			String oNodeType = oLine.substring( nTemp3 + 2, nTemp2 + 1 ).trim();

			// tree������ edge ������ ���,
			if( oLine.contains( "->" ) == true )
			{
				String oTempLine = oLine.substring( oLine.indexOf( "->" ) + 4 );

				nTemp = oTempLine.substring( 1 ).indexOf( "\"" );
				nTemp2 = oTempLine.substring( 1 ).indexOf( "@" );
				nTemp3 = oTempLine.substring( 1 ).indexOf( "." );

				String oSonName = oTempLine.substring( nTemp2 + 2, nTemp + 1 ).trim();
				String oSonType = oTempLine.substring( nTemp3 + 2, nTemp2 + 1 ).trim();

				ScoreTestNode oFromSTN = null;
				ScoreTestNode oToSTN = null;
				LeafNode oToLeaf = null;
				
				// �θ� ��带 ����.(�θ� ���� ������ ScoreTestNode �̴�)
				int nCt1, nCt1Max = oScoreTestNodeSet.size();
				for( nCt1 = 0; nCt1 < nCt1Max; ++nCt1 )
				{
					if( oScoreTestNodeSet.get( nCt1 ).m_oNickName.compareTo( oNodeName ) == 0 )
					{
						oFromSTN = oScoreTestNodeSet.get( nCt1 );						
					}
				}
				
				// �ڽ� ��尡 leaf ����̸�,
				if( oSonType.compareTo( "LeafNode" ) == 0 )
				{
					nCt1Max = oLeafNodeSet.size();
					for( nCt1 = 0; nCt1 < nCt1Max; ++nCt1 )
					{
						if( oLeafNodeSet.get( nCt1 ).m_oNickName.compareTo( oSonName ) == 0 )
						{
							oToLeaf = oLeafNodeSet.get( nCt1 );						
						}
					}
				}
				// �ڽ� ��尡 leaf ��尡 �ƴϸ�,
				else
				{
					nCt1Max = oScoreTestNodeSet.size();
					for( nCt1 = 0; nCt1 < nCt1Max; ++nCt1 )
					{
						if( oScoreTestNodeSet.get( nCt1 ).m_oNickName.compareTo( oSonName ) == 0 )
						{
							oToSTN = oScoreTestNodeSet.get( nCt1 );						
						}
					}
				}
				
				if( oFromSTN.son( 0 ) instanceof OpenNode )
				{
					if( oToSTN != null )
					{
						oFromSTN.replaceSon( oFromSTN.son( 0 ), oToSTN );
						oToSTN.setFather( oFromSTN );
					}
					else
					{
						oFromSTN.replaceSon( oFromSTN.son( 0 ), oToLeaf );
						oToLeaf.setFather( oFromSTN );
					}
				}
				else
				{
					if( oToSTN != null )
					{
						oFromSTN.replaceSon( oFromSTN.son( 1 ), oToSTN );
						oToSTN.setFather( oFromSTN );
						
						//oFromSTN.setHasOpenNode( false );
					}
					else
					{
						oFromSTN.replaceSon( oFromSTN.son( 1 ), oToLeaf );
						oToLeaf.setFather( oFromSTN );
						
						//oFromSTN.setHasOpenNode( false );
						//oToLeaf.updateHasOpenNode();
					}
					
				}
				
				
			}		// edge ����ϴ� if��.	

		}  // ��ü for��.    	
	}

	/** Dot �������� ǥ���� tree string���κ��� Decision tree�� �����ϴ� ������ */
	public DecisionTree( String oDotTree )
	{
		String [] aoSplit = oDotTree.split( "\n" );
		int nTemp = Integer.valueOf( aoSplit[ 0 ].trim() ).intValue();
		
		int nIdx = oDotTree.indexOf( aoSplit[ nTemp + 1 ] );
		
		String oExpForAttSet = oDotTree.substring( 0, nIdx );
		String oExpForDT = oDotTree.substring( nIdx );
		
		System.out.println( "------------------------------------------");
		System.out.println(oExpForAttSet);
		System.out.println( "------------------------------------------");
		System.out.println(oExpForDT);
		System.out.println( "------------------------------------------");
		
		setAttributeSetByExpression( oExpForAttSet );		
		oDotTree = oExpForDT;
		
		anchor = new AnchorNode(this);
		// goalAttribute �� �Ʒ��� loadAttributeSet���� ������
		goalAttribute = null;
		attributeSet = loadAttributeSet( oDotTree );
		
		// ScoreTestNode ���� ���͸� ����.
		Vector<ScoreTestNode> oScoreTestNodeSet = loadScoreTestNodeSet( oDotTree );
		// LeafNode ���� ���͸� ����.
		Vector<LeafNode> oLeafNodeSet = loadLeafNodeSet( oDotTree );
		
		loadEdgeSet(oDotTree, oScoreTestNodeSet, oLeafNodeSet);
		
//		int nCt, nCtMax = oScoreTestNodeSet.size();
//		for( nCt = 0; nCt < nCtMax; ++nCt )
//		{
//			System.out.println( oScoreTestNodeSet.get( nCt ) + "\tNickname : " + oScoreTestNodeSet.get( nCt ).m_oNickName );
//			if( (Node)oScoreTestNodeSet.get( nCt ) instanceof OpenNode )
//			{
//				System.out.println( " - OpenNode" );				
//			}
//			System.out.println( "\t" + oScoreTestNodeSet.get( nCt ).son( 0 ).m_oNickName );
//			System.out.println( "\t" + oScoreTestNodeSet.get( nCt ).son( 1 ).m_oNickName );
//		}
//		
//		System.out.println( "\n\n" + this.hasOpenNode() );
//		System.out.println( this.openNode() );
//		System.out.println( this.root() );
		
//		int nCt, nCtMax = oLeafNodeSet.size();
//		for( nCt = 0; nCt < nCtMax; ++nCt )
//		{
//			System.out.println( oLeafNodeSet.get( nCt ).m_oNickName );
//			if( (Node)oScoreTestNodeSet.get( nCt ) instanceof OpenNode )
//			{
//				System.out.println( " - OpenNode" );				
//			}
//		}
	}


	/**
	 * Guess goal attribute value of an item.
	 *
	 * @param item The item compatible with the tree attribute set.
	 * @return The goal attribute value, or -1 if the matching leaf node does
	 *         not define a goal attribute.
	 **/
	public KnownSymbolicValue guessGoalAttribute(Item item) {
		double[] distribution = goalValueDistribution(item);

		int index = -1;
		double max = -1.;

		for (int i = 0; i < distribution.length; i++)
			if (distribution[i] > max) {
				index = i;
				max = distribution[i];
			}

		return new KnownSymbolicValue(index);
	}


	/**
	 * Finds the leaf/open node matching an item.  All the (tested) attributes
	 * of the item must be known.
	 *
	 * @param item An item compatible with the tree attribute set.
	 * @return The leaf node matching <code>item</code>.
	 **/
	public Node leafNode(Item item) {
		if (getAttributeSet() == null || getGoalAttribute() == null)
			throw new CannotCallMethodException("No attribute set or goal " +
					"attribute defined");

		AttributeSet attributeSet = getAttributeSet();
		Node node = root();

		while (!(node.isLeaf())) {
			TestNode testNode = (TestNode) node;

			int testAttributeIndex =
					attributeSet.indexOf(testNode.test().attribute);

			node = testNode.
					matchingSon(item.valueOf(testAttributeIndex));
		}

		return node;
	}


	/**
	 * Finds the goal value distribution matching an item.  This distribution
	 * describes the probability of each potential goal value for this item.
	 *
	 * @param item An item compatible with the tree attribute set.
	 * @return The goal attribute value distribution for the item
	 *         <code>item</code>.
	 **/
	public double[] goalValueDistribution(Item item) {
		return goalValueDistribution(item, root());
	}


	protected double[] goalValueDistribution(Item item, Node node) {
		if (node.isLeaf())
			return ((LeafNode) node).getGoalValueDistribution();
		else
			if (node instanceof TestNode) {
				TestNode testNode = (TestNode) node;

				int testAttributeIndex = 
						attributeSet.indexOf(testNode.test().attribute);

				if (item.valueOf(testAttributeIndex).isUnknown()) {
					double[] distribution = 
							new double[getGoalAttribute().nbValues];

					Arrays.fill(distribution, 0.);

					for (int i = 0; i < testNode.nbSons(); i++)
						add(distribution,
								times(goalValueDistribution(item, testNode.son(i)),
										testNode.son(i).weight));

					times(distribution, 1. / testNode.weight);

					return distribution;
				} else {
					Node nextNode = 
							testNode.matchingSon(item.valueOf(testAttributeIndex));

					return goalValueDistribution(item, nextNode);
				}
			} else
				throw new CannotCallMethodException("Open node found while " +
						"exploring tree");
	}


	/**
	 * Returns the root node.
	 *
	 * @return This tree's root node.
	 */
	public Node root() {
		return anchor.son();
	}


	/**
	 * Check if a given node is the root node.
	 * 
	 * @param node the node to check.
	 * @return <code>true</code> iff the argument is the root node of this tree.
	 **/
	public boolean isRoot(Node node) {
		if (node == null)
			throw new IllegalArgumentException("Invalid 'null' argument");

		return (node.equals(root()));
	}


	/**
	 * Change this tree's attribute set. The set of attributes of the items
	 * given to this tree. 
	 *
	 * @param attributeSet The new attribute set. Can be set to 'null'.
	 **/
	public void setAttributeSet(AttributeSet attributeSet) {
		this.attributeSet = attributeSet;
	}


	/**
	 * Returns this tree's attribute set.
	 *
	 * @return This tree's attribute set.  A 'null' value means thaht the set is
	 *         undefined.
	 **/
	public AttributeSet getAttributeSet() {
		return attributeSet;
	}


	/**
	 * Change this tree's goal attribute. The goal attribute is the attribute
	 * guessed by the tree.  
	 *
	 * @param goalAttribute The new tree's goal attribute.  Can be set to
	 *                      'null' if unknown.
	 **/
	public void setGoalAttribute(SymbolicAttribute goalAttribute) {
		this.goalAttribute = goalAttribute;
	}


	/**
	 * Get this tree's goal attribute. The goal attribute is the attribute
	 * guessed by the tree.
	 *
	 * @return The tree's goal attribute.  Returns 'null' if unknown.
	 **/
	public SymbolicAttribute getGoalAttribute() {
		return goalAttribute;
	}


	/**
	 * Returns the leftmost open node of the tree.
	 * 'Leftmost' means that the son chosen at each test node while
	 * descending the tree is the smallest number.
	 *
	 * @return The leftmost open node of the tree, or <code>null</code> if the
	 *         tree has no open node.
	 **/
	public OpenNode openNode() {
		return anchor.openNode();
	}


	/**
	 * Checks if the tree has open nodes.
	 *
	 * @return <code>true</code> iff the tree has open nodes.
	 **/
	public boolean hasOpenNode() {
		return anchor.hasOpenNode();
	}


	/**
	 * Returns the nodes of the tree.  The iterator returns the node according
	 * to a breadth first search.  The sons of a node are returned left to
	 * right (i.e. with increasing son number).
	 *
	 * @return An iterator over the tree's nodes.
	 */
	public Iterator breadthFirstIterator() {
		return new DecisionTreeBFIterator(root());
	}


	private double[] times(double[] distribution, double weight) {
		for (int i = 0; i < distribution.length; i++)
			distribution[i] *= weight;

		return distribution;
	}


	private double[] add(double[] d1, double[] d2) {
		if (d1.length != d2.length)
			throw new IllegalArgumentException("distributions must have " +
					"the same number of elements");

		for (int i = 0; i < d1.length; i++)
			d1[i] += d2[i];

		return d1;
	}
}
