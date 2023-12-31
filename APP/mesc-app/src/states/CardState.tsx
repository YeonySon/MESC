import {atom} from 'recoil';

export const cardState = atom<Card>({
  key: 'cardState', // 고유한 키
  default: {
    // 기본값 설정
    cardId: 0,
    content: null,
    cardType: '',
    // 다른 필드는 선택적이므로 초기값이 없어도 됩니다.
  },
});

export interface Card {
  cardId: number;
  cardName?: string | null | undefined;
  content?: string | null;
  cardType: string;
  title?: string | null | undefined;
  labels?: LabelItem[] | null | undefined;
  table?: TableData | null | undefined;
  button?: ButtonItem[] | null | undefined;
  logs?: string | null | undefined;
  isLastPage?: boolean | null | undefined;
  rowCnt?: number | null | undefined;
  totalCnt?: number | null | undefined;
}

type LabelItem = {
  name: string;
  labelType: string;
  query: string;
};

type TableData = {
  columnNameList: string[];
  columnTypeList: string[];
  rowList: string[][];
};

type ButtonItem = {
  id: number;
  name: string;
  linkType: string;
  link: string;
  iconId?: any | null;
  response: string;
};
