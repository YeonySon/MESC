import React, {useState, useEffect, useRef} from 'react';
import {
  View,
  Dimensions,
  ScrollView,
  TouchableOpacity,
  Modal,
  Text,
} from 'react-native';
import * as S from './Teble.styles';
import {useRecoilValue} from 'recoil';
import {TableTitleState} from '../../../states/DataTitleState';
import DataBox from './DataBox';

type TableProps = {
  title?: string;
  columnName: string[];
  columnType: string[];
  rowList: any[][];
};

const Table: React.FC<TableProps> = ({
  title,
  columnName,
  columnType,
  rowList,
}) => {
  const [columnWidths, setColumnWidths] = useState<number[]>(
    new Array(columnName.length).fill(0),
  );
  const [isModalVisible, setModalVisible] = useState(false);
  const [selectedRow, setSelectedRow] = useState<{
    rowIndex: number;
    content: string[];
  } | null>(null);

  // 셀 클릭 이벤트 핸들러
  const handleRowPress = (rowIndex: any, row: any) => {
    console.log(`Row ${rowIndex} was pressed`, row);
    setSelectedRow({rowIndex, content: row});
    setModalVisible(true);
  };

  // 모달 숨기는 함수
  const hideModal = () => setModalVisible(false);

  const dataTitle = useRecoilValue(TableTitleState);

  const horizontalScrollRef = useRef(null);

  const tableHeader = makeHeader(title);

  const updateColumnWidths = (width: number, index: number) => {
    setColumnWidths(currentWidths => {
      const newWidths = [...currentWidths];
      newWidths[index] = Math.max(newWidths[index], width);
      return newWidths;
    });
  };

  // 각 열에 대해 onLayout 콜백을 생성하는 함수
  const onLayoutCallback = (index: number) => (event: any) => {
    const {width} = event.nativeEvent.layout;
    updateColumnWidths(width, index);
  };

  function makeHeader(title: String | undefined) {
    if (!title) return <></>;
    return (
      <S.Header>
        <S.Title>{title}</S.Title>
        <S.Button></S.Button>
      </S.Header>
    );
  }

  // // ScrollView의 너비를 화면 크기에 맞추기 위한 state
  // const [scrollViewWidth, setScrollViewWidth] = useState(0);

  useEffect(() => {
    const screen = Dimensions.get('window');
    // setScrollViewWidth(screen.width);
  }, []);

  return (
    <S.Container>
      {tableHeader}
      <S.Body>
        <ScrollView horizontal={true} showsHorizontalScrollIndicator={false}>
          <View>
            <View style={{flexDirection: 'row'}}>
              {columnName.map((column, index) => (
                <S.ColumnInfoBox key={`header-${index}`}>
                  <S.ColumnName>{column}</S.ColumnName>
                </S.ColumnInfoBox>
              ))}
            </View>
            <View style={{flexDirection: 'row'}}>
              {columnType.map((type, index) => (
                <S.ColumnInfoBox key={`type-${index}`}>
                  <S.ColumnType>{type}</S.ColumnType>
                </S.ColumnInfoBox>
              ))}
            </View>
            <ScrollView
              nestedScrollEnabled={true}
              showsVerticalScrollIndicator={false}>
              {rowList.map((row, rowIndex) => (
                <TouchableOpacity
                  key={`row-${rowIndex}`}
                  style={{flexDirection: 'row'}}
                  onPress={() => handleRowPress(rowIndex, row)}>
                  {row.map((cell, cellIndex) => (
                    <S.CellBox key={`cell-${rowIndex}-${cellIndex}`}>
                      <S.Cell>{cell}</S.Cell>
                    </S.CellBox>
                  ))}
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>
        </ScrollView>
      </S.Body>
      <Modal
        animationType="slide"
        transparent={true}
        visible={isModalVisible}
        onRequestClose={hideModal}>
        <S.ModalContainer>
          {/* 모달을 닫는 버튼 */}
          <TouchableOpacity onPress={hideModal}>
            <Text>Close</Text>
          </TouchableOpacity>
          <Table
            title={title}
            columnName={columnName}
            columnType={columnType}
            rowList={rowList}
          />
        </S.ModalContainer>
      </Modal>
    </S.Container>
  );
};
export default Table;
