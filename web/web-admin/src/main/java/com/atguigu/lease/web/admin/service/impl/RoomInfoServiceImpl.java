package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.v3.oas.annotations.media.Schema;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {


    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;
    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Override
    public void saveOrUpdateVo(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getApartmentId() != null;
        super.saveOrUpdate(roomSubmitVo);
        if (isUpdate) {
            //1. 删除图片列表
            LambdaQueryWrapper<GraphInfo> graphInfoWrapper = new LambdaQueryWrapper<>();
            graphInfoWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphInfoService.remove(graphInfoWrapper);
            //2. 删除属性信息列表
            LambdaQueryWrapper<RoomAttrValue> roomAttrValueWrapper = new LambdaQueryWrapper<>();
            roomAttrValueWrapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(roomAttrValueWrapper);
            //3. 删除配套信息列表
            LambdaQueryWrapper<RoomFacility> roomFacilityWrapper = new LambdaQueryWrapper<>();
            roomFacilityWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(roomFacilityWrapper);
            //4. 删除标签信息列表
            LambdaQueryWrapper<RoomLabel> roomLabelWrapper = new LambdaQueryWrapper<>();
            roomLabelWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(roomLabelWrapper);
            //5. 删除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> roomPaymentWrapper = new LambdaQueryWrapper<>();
            roomPaymentWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(roomPaymentWrapper);
            //6. 删除可选租期列表
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(roomLeaseTermWrapper);
        }
        //1. 保存图片列表
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)) {
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setName(graphVo.getName());
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setItemType(ItemType.ROOM);
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }

        //2. 保存属性信息列表
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if (!CollectionUtils.isEmpty(attrValueIds)) {
            ArrayList<RoomAttrValue> attrValueList = new ArrayList<>();
            for (Long attrValueId : attrValueIds) {
                RoomAttrValue roomAttrValue = new RoomAttrValue(roomSubmitVo.getId(), attrValueId);
                attrValueList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(attrValueList);
        }

        //3. 保存配套信息列表
        List<Long> facilityIdList = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityIdList)) {
            ArrayList<RoomFacility> facilityInfoList = new ArrayList<>();
            for (Long id : facilityIdList) {
                RoomFacility roomFacility = RoomFacility.builder()
                                                        .roomId(roomSubmitVo.getId())
                                                        .facilityId(id)
                                                        .build();
                facilityInfoList.add(roomFacility);
            }
            roomFacilityService.saveBatch(facilityInfoList);
        }
        //4. 保存标签信息列表
        List<Long> ids = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(ids)){
            ArrayList<RoomLabel> roomLabelList = new ArrayList<>();
            for (Long id : ids) {
                RoomLabel roomLabel = RoomLabel.builder()
                                                .roomId(roomSubmitVo.getId())
                                                .labelId(id)
                                                .build();
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }
        //5. 保存支付方式列表
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if(!CollectionUtils.isEmpty(paymentTypeIds)){
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for (Long id : paymentTypeIds) {
                RoomPaymentType roomPaymentType = RoomPaymentType.builder()
                        .roomId(roomSubmitVo.getId())
                        .paymentTypeId(id)
                        .build();
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }
        //6. 保存可选租期列表
        List<Long> termIds = roomSubmitVo.getLeaseTermIds();
        if(!CollectionUtils.isEmpty(termIds)){
            ArrayList<RoomLeaseTerm> roomLeaseTermList = new ArrayList<>();
            for (Long id : termIds) {
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder()
                        .roomId(roomSubmitVo.getId())
                        .leaseTermId(id)
                        .build();
                roomLeaseTermList.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTermList);
        }

    }

    @Override
    public IPage<RoomItemVo> pageRoomItemByQuery(IPage<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageRoomItemByQuery(page,queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo=roomInfoMapper.selectById(id);
        //2.查询所属公寓信息
        ApartmentInfo apartmentInfo=apartmentInfoService.getById(roomInfo.getApartmentId());
        //3. 图片列表
        List<GraphVo> graphVoList=graphInfoMapper.selectListByTypeAndId(ItemType.ROOM,id);

        //@"属性信息列表")
        List<AttrValueVo> attrValueVoList=attrValueMapper.selectVoListByTypeAndId(ItemType.ROOM,id);

        //@ "配套信息列表")
        List<FacilityInfo> facilityInfoList=facilityInfoMapper.selectListByRoomId(id);

        //@"标签信息列表")
        List<LabelInfo> labelInfoList=labelInfoMapper.selectListByRoomId(id);

        //@"支付方式列表")
        List<PaymentType> paymentTypeList=paymentTypeMapper.selectListByRoomId(id);

        //@"可选租期列表")
        List<LeaseTerm> leaseTermList=leaseTermMapper.selectListByRoomId(id);

        RoomDetailVo roomDetailVo = new RoomDetailVo();
        //把继承关系里的值复制过去
        BeanUtils.copyProperties(roomInfo,roomDetailVo);
        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(graphVoList);
        roomDetailVo.setAttrValueVoList(attrValueVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);

        return roomDetailVo;
    }

    @Override
    public void removeVoById(Long id) {
        super.removeById(id);
        //1. 删除图片列表
        LambdaQueryWrapper<GraphInfo> graphInfoWrapper = new LambdaQueryWrapper<>();
        graphInfoWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphInfoWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(graphInfoWrapper);
        //2. 删除属性信息列表
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueWrapper = new LambdaQueryWrapper<>();
        roomAttrValueWrapper.eq(RoomAttrValue::getRoomId,id);
        roomAttrValueService.remove(roomAttrValueWrapper);
        //3. 删除配套信息列表
        LambdaQueryWrapper<RoomFacility> roomFacilityWrapper = new LambdaQueryWrapper<>();
        roomFacilityWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(roomFacilityWrapper);
        //4. 删除标签信息列表
        LambdaQueryWrapper<RoomLabel> roomLabelWrapper = new LambdaQueryWrapper<>();
        roomLabelWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(roomLabelWrapper);
        //5. 删除支付方式列表
        LambdaQueryWrapper<RoomPaymentType> roomPaymentWrapper = new LambdaQueryWrapper<>();
        roomPaymentWrapper.eq(RoomPaymentType::getRoomId,id);
        roomPaymentTypeService.remove(roomPaymentWrapper);
        //6. 删除可选租期列表
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermWrapper.eq(RoomLeaseTerm::getRoomId,id);
        roomLeaseTermService.remove(roomLeaseTermWrapper);



    }
}




